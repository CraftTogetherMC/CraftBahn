package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.generated.net.minecraft.world.entity.EntityHandle;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.net.TCPClient;
import de.crafttogether.craftbahn.net.TCPServer;
import de.crafttogether.craftbahn.net.events.EntityReceivedEvent;
import de.crafttogether.craftbahn.net.events.PacketReceivedEvent;
import de.crafttogether.craftbahn.net.packets.EntityPacket;
import de.crafttogether.craftbahn.net.packets.MessagePacket;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    private final Map<MinecartGroup, Portal> receivedTrains = new HashMap<>();

    public PortalHandler(String host, int port) {
        // Create Server Socket
        this.tcpServer = new TCPServer(host, port);

        // Register as EventHandler
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Register TrainCarts-ActionSigns
        registerActionSigns();
    }

    public void handleTrain(SignActionEvent event) {
        Util.debug("#handleTrain");

        MinecartGroup group = event.getGroup();
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

        // Abort if the triggered sign is the sign where the train was spawned
        if (receivedTrains.containsKey(group) && receivedTrains.get(group).getName().equals(targetPortal.getName()))
            return;

        if (targetPortal == null || targetPortal.getTargetLocation() == null) {
            TCHelper.sendMessage(group, Localization.PORTAL_ENTER_NOEXIT,
                    PlaceholderResolver.resolver("name", portalName));
            return;
        }

        Util.debug(event.getGroup().getProperties().getTrainName() + " goes from " + plugin.getServerName() + " (" + event.getLine(2) + " -> " + event.getLine(3) + ") to " + targetPortal.getTargetLocation().getServer() + " (" + targetPortal.getName() + " -> " + targetPortal.getType().name() + ")");

        // Should we clear chest-minecarts?
        boolean clearInventory = event.getLine(3).equalsIgnoreCase("clear");

        // Try to transfer train to the target server
        if(!transferTrain(group, targetPortal, clearInventory))
            return;

        // Cache teleportation-infos
        pendingTeleports.put(group, targetPortal);

        // Apply blindness-effect
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1);
        List<Player> playerPassengers = TCHelper.getPlayerPassengers(group);
        for (Player passenger : playerPassengers)
            passenger.addPotionEffect(blindness);

        // Disable collisions after train is sent to avoid they're being pushed back by players
        group.getProperties().setCollision(CollisionOptions.CANCEL);
    }

    public void handleCart(SignActionEvent event) {
        Util.debug("#handleCart");

        MinecartGroup group = event.getGroup();
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

        group.getProperties().setSpawnItemDrops(false);

        // Destroy cart per cart
        if (group.size() <= 1) {
            group.destroy();
            pendingTeleports.remove(group);
        }
        else {
            Entity cartEntity = member.getEntity().getEntity();
            group.removeSilent(member);
            cartEntity.remove();
        }
    }

    // Handle joined player if he was a passenger
    public static void reEnterPassenger(Passenger passenger, PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        int cartIndex = passenger.getCartIndex();

        // Try to find train and set player as passenger
        MinecartGroup train = TCHelper.getTrain(passenger.getTrainName());
        if (train == null) {
            Util.debug("no train found for passenger: " + player.getName());
            player.sendMessage("Es wurde kein Zug gefunden");
            return;
        }

        MinecartMember<?> member = train.get(cartIndex);

        if (member instanceof MinecartMemberRideable) {
            event.setSpawnLocation(member.getEntity().getLocation());
            member.getEntity().setPassenger(player);
            Passenger.remove(passenger.getUUID());
        }
        else
            Util.debug("Unable to re-enter " + player.getName() + " because the cart is not rideable");

        Passenger.remove(passenger.getUUID());
    }

    public boolean transferTrain(MinecartGroup group, Portal portal, boolean clearInventory) {
        Util.debug("#transferTrain");

        // Save train and get properties
        ConfigurationNode trainProperties = group.saveConfig();

        // Remove lastPathNode from cartProperties
        Set<ConfigurationNode> cartNodes = trainProperties.getNode("carts").getNodes();
        for (ConfigurationNode node : cartNodes) {
            if (node.contains("lastPathNode"))
                node.set("lastPathNode", "");
        }

        // TODO: Clear Inventory

        // Get passengers and seat numbers
        List<Passenger> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group) {
            for (Entity entity : TCHelper.getPassengers(member)) {
                Passenger passenger = new Passenger(entity.getUniqueId(), entity.getType(), member.getIndex());
                passengers.add(passenger);
            }
        }

        TrainPacket packet = new TrainPacket();
        packet.name = group.getProperties().getTrainName();
        packet.owners = group.getProperties().getOwners();
        packet.properties = trainProperties.toString();
        packet.passengers = passengers;
        packet.portalName = portal.getName();
        packet.sourceServer = plugin.getServerName();
        packet.target = portal.getTargetLocation();

        // Transfer train
        TCPClient client = new TCPClient(portal.getTargetHost(), portal.getTargetPort());
        client.sendAuth(plugin.getConfig().getString("Portals.Server.SecretKey"));

        boolean success = client.send(packet);
        client.disconnect();

        return success;
    }

    @EventHandler
    public void receivePacket(PacketReceivedEvent event) {
        if (event.getPacket() instanceof TrainPacket)
            receiveTrain((TrainPacket) event.getPacket());
    }

    public void receiveTrain(TrainPacket packet) {
        Util.debug("TrainPacket received", false);

        // Check if world exists
        World targetWorld = Bukkit.getWorld(packet.target.getWorld());
        if (targetWorld == null) {
            Util.debug("World '" + packet.target.getWorld() + "' was not found!");
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        // Load train from received config
        ConfigurationNode trainConfig = new ConfigurationNode();
        trainConfig.loadFromString(packet.properties);
        SpawnableGroup spawnable = SpawnableGroup.fromConfig(TrainCarts.plugin, trainConfig);

        Location targetLocation = packet.target.getBukkitLocation();
        Portal portal = plugin.getPortalStorage().getPortal(targetLocation);
        if (portal == null || portal.getSign() == null) {
            Util.debug("Portal-Sign was not found at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        // No Rail!
        RailPiece rail = RailLookup.discoverRailPieceFromSign(portal.getSign().getBlock());
        if (rail == null) {
            Util.debug("Rail was not found at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        SpawnableGroup.SpawnLocationList spawnLocations = TCHelper.getSpawnLocations(spawnable, rail, portal.getSign());
        if (spawnLocations == null) {
            Util.debug("Couldn't find the right spot to spawn a train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        // Load the chunks first
        spawnLocations.loadChunks();

        // Check that the area isn't occupied by another train
        if (spawnLocations.isOccupied()) {
            Util.debug("Track is occupied at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
            // TODO: Inform players
            //Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        // Spawn
        MinecartGroup group = spawnable.spawn(spawnLocations);
        receivedTrains.put(group, portal);

        // Rename
        String newName = packet.name;
        if (!group.getProperties().getTrainName().equals(packet.name) && TCHelper.getTrain(packet.name) != null)
            newName = TrainProperties.generateTrainName(packet.name + "#");
        group.getProperties().setTrainName(newName);

        // Process passengers
        for (Passenger passenger : packet.passengers)
            Passenger.register(passenger, newName);

        // Launch
        Vector headDirection = spawnLocations.locations.get(spawnLocations.locations.size()-1).forward;
        BlockFace launchDirection = com.bergerkiller.bukkit.tc.Util.vecToFace(headDirection, false);
        group.head().getActions().addActionLaunch(launchDirection, 2, 0.4);

        Util.debug(group.getProperties().getTrainName() + " came from " + packet.sourceServer + " (" + packet.portalName + ") to " + plugin.getServerName() + " (" + portal.getSign().getLine(1) + " - " + portal.getSign().getLine(2) + ")");
        Util.debug("train spawned: " + newName + " #");
        Util.debug("launchDirection: " + launchDirection.name());
    }

    public void sendEntityToServer(LivingEntity entity, Portal portal) {
        EntityHandle entityHandle = EntityHandle.fromBukkit(entity);
        CommonTagCompound tagCompound = new CommonTagCompound();
        entityHandle.saveToNBT(tagCompound);
        entity.remove();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TCPClient client = new TCPClient(portal.getTargetHost(), portal.getTargetPort());
            client.sendAuth(plugin.getConfig().getString("Portals.Server.SecretKey"));
            client.send(new EntityPacket(entity.getUniqueId(), entity.getType()));

            OutputStream outputStream = client.getOutputStream();
            if (outputStream == null)
                Util.debug("OutputStream is NULL");

            try {
                tagCompound.writeToStream(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            client.disconnect();
        });
    }

    @EventHandler
    public void receiveEntity(EntityReceivedEvent event) {
        Util.debug("received entity");

        Passenger passenger = Passenger.get(event.getUuid());
        MinecartGroup train = TCHelper.getTrain(passenger.getTrainName());

        if (train == null) {
            Util.debug("no train found for entity");
            return;
        }

        // Spawn Entity
        Location location = train.head().getLastBlock().getLocation().add(0, 1, 0);
        Entity spawnedEntity = location.getWorld().spawnEntity(location, event.getType());

        // Load received NBT to spawned Entity

        Util.debug("Load NBT....");

        EntityHandle entityHandle = EntityHandle.fromBukkit(spawnedEntity);
        entityHandle.loadFromNBT(event.getTagCompound());
        Util.debug("NBT Loaded!");

        Util.debug("Set as passenger");
        MinecartMember<?> cart = train.get(passenger.getCartIndex());
        cart.getEntity().setPassenger(spawnedEntity);

        Passenger.remove(passenger.getUUID());
    }

    public void sendPlayerToServer(Player player, Portal targetPortal) {
        Util.debug("send player " + player + " to " + targetPortal.getTargetLocation().getServer());
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
