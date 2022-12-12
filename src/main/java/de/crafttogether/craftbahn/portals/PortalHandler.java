package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.tc.SignActionHeader;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.net.packets.AuthenticationPacket;
import de.crafttogether.craftbahn.net.events.PacketReceivedEvent;
import de.crafttogether.craftbahn.net.TCPClient;
import de.crafttogether.craftbahn.net.TCPServer;
import de.crafttogether.craftbahn.net.packets.TrainPacket;
import de.crafttogether.craftbahn.signactions.SignActionPortal;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import de.crafttogether.craftbahn.util.TCHelper;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class PortalHandler implements Listener {
    private static final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;
    private static final SignActionPortal signActionPortal = new SignActionPortal();
    private static final SignActionPortalIn signActionPortalIn = new SignActionPortalIn();
    private static final SignActionPortalOut signActionPortalOut = new SignActionPortalOut();

    private final TCPServer tcpServer;
    private final Map<MinecartGroup, Portal> pendingTeleports = new HashMap<>();
    private final Map<MinecartGroup, String> receivedTrains = new HashMap<>();

    public PortalHandler(String host, int port) {
        // Create Server Socket
        this.tcpServer = new TCPServer(host, port);

        // Register as EventHandler
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Register TrainCarts-ActionSigns
        registerActionSigns();
    }

    public void handleTrain(SignActionEvent event) {
        MinecartGroup group = event.getGroup();

        // Abort if source-server equals actual server
        if (receivedTrains.containsKey(group) && receivedTrains.get(group).equals(plugin.getServerName()))
            return;

        Util.debug("#handleTrain");

        String portalName = event.getLine(2);
        Portal.PortalType targetType = null;

        if (event.getLine(1).equals("portal"))
            targetType = Portal.PortalType.BIDIRECTIONAL;
        else if (event.getLine(1).equals("portal-in"))
            targetType = Portal.PortalType.OUT;

        // Get existing portal-out -signs from database
        List<Portal> portals;
        try {
            Portal.PortalType finalTargetType = targetType;
            portals = plugin.getPortalStorage().get(portalName).stream()
                    .filter(portal -> portal.getType().equals(finalTargetType))
                    .filter(portal -> !portal.getTargetLocation().getServer().equals(plugin.getServerName()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            TCHelper.sendMessage(group, Localization.COMMAND_ERROR,
                    PlaceholderResolver.resolver("error", e.getMessage()));
            e.printStackTrace();
            return;
        }
        Portal targetPortal = portals.get(0);

        if (targetPortal == null || targetPortal.getTargetLocation() == null) {
            TCHelper.sendMessage(group, Localization.PORTAL_ENTER_NOEXIT,
                    PlaceholderResolver.resolver("name", portalName));
            return;
        }

        Util.debug(event.getGroup().getProperties().getTrainName() + " goes from " + plugin.getServerName() + " to " + targetPortal.getTargetLocation().getServer() + " (" + event.getLine(1) + ")");

        // Apply blindness-effect
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1);
        List<Player> playerPassengers = TCHelper.getPlayerPassengers(group);
        for (Player passenger : playerPassengers)
            passenger.addPotionEffect(blindness);

        // Clear Inventory if needed
        if (event.getLine(3).equalsIgnoreCase("clear"))
            TCHelper.clearInventory(group);

        // Cache teleportation-infos
        pendingTeleports.put(group, targetPortal);

        // Transfer train-properties to target server
        transferTrain(group, targetPortal);

        // Disable collisions after train is sent to avoid they're being pushed back by players
        group.getProperties().setCollision(CollisionOptions.CANCEL);
    }

    public void handleCart(SignActionEvent event) {
        MinecartGroup group = event.getGroup();

        // Abort if source-server equals actual server
        if (receivedTrains.containsKey(group) && receivedTrains.get(group).equals(plugin.getServerName()))
            return;

        Util.debug("#handleCart");
        Portal portal = pendingTeleports.get(group);

        // Abort if no pendingTeleport was created
        if (portal == null)
            return;

        // Transfer passengers to target server
        MinecartMember<?> member = event.getMember();
        for (Entity passenger : member.getEntity().getEntity().getPassengers()) {
            if (passenger instanceof Player)
                sendPlayerToServer((Player) passenger, portal);

            else if (passenger instanceof LivingEntity)
                sendEntityToServer((LivingEntity) passenger, portal);
        }

        // Destroy cart per cart
        if (group.size() <= 1) {
            group.destroy();
        }
        else {
            Entity cartEntity = member.getEntity().getEntity();
            group.removeSilent(member);
            cartEntity.remove();
        }
    }

    // Handle joined player if he was a passenger
    public static void reEnterPassenger(Passenger passenger, PlayerSpawnLocationEvent e) {
        Player player = e.getPlayer();
        int cartIndex = passenger.getCartIndex();

        // Try to find train and set player as passenge
        MinecartGroup train = passenger.getTrain();
        if (train == null)
            return;

        MinecartMember<?> member = train.get(cartIndex);

        if (member instanceof MinecartMemberRideable) {
            if (player.isFlying())
                player.setFlying(false);

            e.setSpawnLocation(member.getEntity().getLocation());
            //player.teleport(cart.getEntity().getLocation());
            member.getEntity().setPassenger(player);
            Passenger.remove(passenger.getUUID());
        }
    }

    public void transferTrain(MinecartGroup group, Portal portal) {
        Util.debug("#transferTrain");

        // Connect TCPClient
        String ip = plugin.getConfig().getString("Portals.Server.PublicAddress");
        int port = plugin.getConfig().getInt("Portals.Server.Port");
        Util.debug(ip + ":" + port + " connecting to " + portal.getTargetHost() + ":" + portal.getTargetPort() + " targetServer: " + portal.getTargetLocation().getServer());
        TCPClient client = new TCPClient(portal.getTargetHost(), portal.getTargetPort());

        // Save train and get properties
        ConfigurationNode trainProperties = group.saveConfig();

        // Remove lastPathNode from cartProperties
        Set<ConfigurationNode> cartNodes = trainProperties.getNode("carts").getNodes();
        for (ConfigurationNode node : cartNodes) {
            if (node.contains("lastPathNode"))
                node.set("lastPathNode", "");
        }

        // Generate unique id and new name
        String trainID = UUID.randomUUID().toString().split("-")[0];
        String trainNewName = CraftBahnPlugin.plugin.getServerName() + "-" + group.getProperties().getTrainName();

        // Get passengers and seat numbers
        List<Passenger> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group) {
            for (Entity entity : TCHelper.getPassengers(member)) {
                Passenger passenger = new Passenger(entity.getUniqueId(), entity.getType().name(), member.getIndex());
                passengers.add(passenger);
            }
        }

        TrainPacket trainPacket = new TrainPacket();
        trainPacket.id = trainID;
        trainPacket.name = group.getProperties().getTrainName();
        trainPacket.newName = trainNewName;
        trainPacket.sourceServer = plugin.getServerName();
        trainPacket.owners = group.getProperties().getOwners();
        trainPacket.properties = trainProperties.toString();
        trainPacket.target = portal.getTargetLocation();
        trainPacket.passengers = passengers;

        // Transfer train
        client.sendAuth(plugin.getConfig().getString("Portals.Server.SecretKey"));
        Util.debug("send train");
        client.send(trainPacket);
        client.disconnect();
    }

    @EventHandler
    public void receivePacket(PacketReceivedEvent event) {
        Util.debug("received packet");

        if (event.getPacket() instanceof TrainPacket) {
            receiveTrain((TrainPacket) event.getPacket());
        }
    }

    public void receiveTrain(TrainPacket trainPacket) {
        Util.debug("TrainPacket received", false);

        // Check if world exists
        World targetWorld = Bukkit.getWorld(trainPacket.target.getWorld());
        if (targetWorld == null) {
            Util.debug("World '" + trainPacket.target.getWorld() + "' was not found!");
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        Location targetLocation = trainPacket.target.getBukkitLocation();

        // Load train from received config
        ConfigurationNode trainConfig = new ConfigurationNode();
        trainConfig.loadFromString(trainPacket.properties);
        SpawnableGroup spawnable = SpawnableGroup.fromConfig(trainConfig);

        Portal portal = plugin.getPortalStorage().getPortal(targetLocation);
        if (portal == null || portal.getSign() == null) {
            Util.debug("Portal-Sign was not found at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        SignActionHeader actionHeader = SignActionHeader.parseFromSign(portal.getSign());
        RailPiece rail = RailLookup.discoverRailPieceFromSign(portal.getSign().getBlock());

        // Try to spawn a train
        if (rail != null) {
            SpawnableGroup.SpawnLocationList spawnLocations = TCHelper.getSpawnLocations(spawnable, rail, portal.getSign());

            if (spawnLocations == null) {
                Util.debug("Couldn't spawn a train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
                // TODO: Inform players
                //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
                return;
            }

            // Spawn and launch
            MinecartGroup group = spawnable.spawn(spawnLocations);
            receivedTrains.put(group, trainPacket.sourceServer);

            Util.debug(group.getProperties().getTrainName() + " came from " + trainPacket.sourceServer + " to " + plugin.getServerName() + " (" + portal.getSign().getLine(1) + ")");

            Vector headDirection = spawnLocations.locations.get(spawnLocations.locations.size()-1).forward;
            BlockFace launchDirection = com.bergerkiller.bukkit.tc.Util.vecToFace(headDirection, false);

            Util.debug("train spawned: " + trainPacket.newName + " #" + trainPacket.id);
            Util.debug("launchDirection: " + launchDirection.name());
            group.head().getActions().addActionLaunch(launchDirection, 2, 0.4);
        }
    }

    public void receiveEntity(UUID uuid, EntityType entityType, CommonTagCompound entityNBT) {

    }

    public void sendEntityToServer(LivingEntity entity, Portal targetPortal) {

    }

    public void sendPlayerToServer(Player player, Portal targetPortal) {
        // Use PluginMessaging to send players to target server
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(targetPortal.getTargetLocation().getServer());
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public void registerActionSigns() {
        SignAction.register(signActionPortal);
        SignAction.register(signActionPortalIn);
        SignAction.register(signActionPortalOut);
    }

    public void unregisterActionSigns() {
        SignAction.unregister(signActionPortal);
        SignAction.unregister(signActionPortalIn);
        SignAction.unregister(signActionPortalOut);
    }

    public void shutdown() {
        // Close server
        if (tcpServer != null)
            tcpServer.close();

        // Close all active clients
        TCPClient.closeAll();

        unregisterActionSigns();
    }

    public Map<MinecartGroup, Portal> getPendingTeleports() {
        return pendingTeleports;
    }
}
