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
import de.crafttogether.craftbahn.net.packets.TrainPacket;
import de.crafttogether.craftbahn.signactions.SignActionPortal;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import de.crafttogether.craftbahn.util.TCHelper;
import de.crafttogether.craftbahn.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PortalHandler implements Listener {
    private static final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;
    private static final SignActionPortal signActionPortal = new SignActionPortal();
    private static final SignActionPortalIn signActionPortalIn = new SignActionPortalIn();
    private static final SignActionPortalOut signActionPortalOut = new SignActionPortalOut();

    private final TCPServer tcpServer;
    private final ConcurrentHashMap<String, Portal> pendingTeleports = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Portal> receivedTrains = new ConcurrentHashMap<>();

    public PortalHandler(String host, int port) {
        // Create Server Socket
        this.tcpServer = new TCPServer(host, port);

        // Register as EventHandler
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Register TrainCarts-ActionSigns
        registerActionSigns();
    }

    public void handleTrain(SignActionEvent event) {
        Util.debug("#handleTrain size: " + event.getGroup().size() + " passengers: " + TCHelper.getPassengers(event.getGroup()).size());

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
        Portal portal = (portals.size() < 1) ? null : portals.get(0);

        // Abort if the triggered sign is the sign where the train was spawned
        String receivedTrain = group.getProperties().getTrainName();
        if (receivedTrains.containsKey(receivedTrain) && receivedTrains.get(receivedTrain).getName().equals(portal.getName()))
            return;

        if (portal == null || portal.getTargetLocation() == null) {
            TCHelper.sendMessage(group, Localization.PORTAL_ENTER_NOEXIT,
                    PlaceholderResolver.resolver("name", portalName));
            return;
        }

        Util.debug(event.getGroup().getProperties().getTrainName() + " goes from " + plugin.getServerName() + " (" + event.getLine(2) + " -> " + event.getLine(3) + ") to " + portal.getTargetLocation().getServer() + " (" + portal.getName() + " -> " + portal.getType().name() + ")");

        // Should we clear chest-minecarts?
        boolean clearInventory = event.getLine(3).equalsIgnoreCase("clear");

        // Try to transfer train to the target server
        if (!transferTrain(group, portal, clearInventory))
            return;

        // Load chunks (try to keep entities alive)
        TCHelper.loadChunks(event.getLocation().getChunk(), 2, 5);

        // Cache teleportation-infos
        for (String trainName : pendingTeleports.keySet()) {
            if (TrainProperties.get(trainName) == null) continue;
            pendingTeleports.remove(trainName);
        }
        pendingTeleports.put(group.getProperties().getTrainName(), portal);

        // Apply blindness-effect
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1);
        List<Player> playerPassengers = TCHelper.getPlayerPassengers(group);
        for (Player passenger : playerPassengers)
            passenger.addPotionEffect(blindness);

        group.getProperties().setCollision(CollisionOptions.CANCEL);
    }

    public void handleCart(SignActionEvent event) {
        Util.debug("#handleCart");

        MinecartGroup group = event.getGroup();
        Portal portal = pendingTeleports.get(group.getProperties().getTrainName());

        // Abort if no pendingTeleport was created
        if (portal == null)
            return;

        // Transfer passengers to target server
        MinecartMember<?> member = event.getMember();

        for (Entity passenger : member.getEntity().getEntity().getPassengers()) {
            if (passenger instanceof Player)
                sendPlayerToServer((Player) passenger, portal);

            else if (passenger instanceof LivingEntity) {
                passenger.remove();
            }
        }

        group.getProperties().setSpawnItemDrops(false);

        // Destroy cart per cart
        if (group.size() <= 1) {
            pendingTeleports.remove(group.getProperties().getTrainName());
            group.destroy();
            group.remove();
        }
        else {
            Entity cartEntity = member.getEntity().getEntity();
            group.removeSilent(member);
            cartEntity.remove();
        }
    }

    public boolean transferTrain(MinecartGroup group, Portal portal, boolean clearInventory) {
        Util.debug("#transferTrain");

        // Generate unique id to identify the train later
        UUID trainId = UUID.randomUUID();

        // Clear every item container
        if (clearInventory)
            TCHelper.clearInventory(group);

        // Save train and get properties
        ConfigurationNode trainProperties = group.saveConfig().clone();
        trainProperties.set("name", "train1");

        // Remove lastPathNode from cartProperties
        Set<ConfigurationNode> cartNodes = trainProperties.getNode("carts").getNodes();
        for (ConfigurationNode node : cartNodes) {
            if (node.contains("lastPathNode"))
                node.set("lastPathNode", "");
        }

        // Get passengers and seat numbers
        List<Passenger> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group) {
            for (Entity entity : TCHelper.getPassengers(member)) {
                Util.debug("Passenger found: " + entity.getType());
                Passenger passenger = new Passenger(trainId, entity.getUniqueId(), entity.getType(), member.getIndex());
                passengers.add(passenger);
                
                if (entity instanceof LivingEntity && !(entity instanceof Player))
                    sendEntityToServer((LivingEntity) entity, portal);
            }
        }

        TrainPacket packet = new TrainPacket();
        packet.id = trainId;
        packet.oldName = group.getProperties().getTrainName();
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

        if (success)
            Util.debug("Train (" + packet.oldName + ") was sent to " + packet.target.getServer() + "!");

        return success;
    }

    @EventHandler
    public void receivePacket(PacketReceivedEvent event) {
        if (event.getPacket() instanceof TrainPacket)
            receiveTrain((TrainPacket) event.getPacket());
    }

    public void receiveTrain(TrainPacket packet) {
        Util.debug("Received train (" + packet.oldName + ") from " + packet.sourceServer, false);

        // Register passengers
        for (Passenger passenger : packet.passengers)
            Passenger.register(passenger);

        // Check if world exists
        World targetWorld = Bukkit.getWorld(packet.target.getWorld());
        if (targetWorld == null) {
            Util.debug("World '" + packet.target.getWorld() + "' was not found!");
            Passenger.error(packet.id, Localization.PORTAL_ENTER_WORLDNOTFOUND.deserialize(
                    PlaceholderResolver.resolver("world", packet.target.getWorld())));
            return;
        }

        // Load train from received config
        ConfigurationNode trainConfig = new ConfigurationNode();
        trainConfig.loadFromString(packet.properties);
        SpawnableGroup spawnable = SpawnableGroup.fromConfig(TrainCarts.plugin, trainConfig);

        // Portal not found
        Location targetLocation = packet.target.getBukkitLocation();
        Portal portal = plugin.getPortalStorage().getPortal(targetLocation);
        if (portal == null || portal.getSign() == null) {
            Util.debug("Could not find a Portal at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            Passenger.error(packet.id, Localization.PORTAL_ENTER_SIGNNOTFOUND.deserialize(
                    PlaceholderResolver.resolver("name", packet.portalName),
                    PlaceholderResolver.resolver("world", packet.target.getWorld()),
                    PlaceholderResolver.resolver("x", String.valueOf(packet.target.getX())),
                    PlaceholderResolver.resolver("y", String.valueOf(packet.target.getY())),
                    PlaceholderResolver.resolver("z", String.valueOf(packet.target.getZ()))));
            return;
        }

        // No Rail!
        RailPiece rail = RailLookup.discoverRailPieceFromSign(portal.getSign().getBlock());
        if (rail == null || rail.block() == null) {
            Util.debug("Could not find a Rail at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            // TODO: Inform players
            Passenger.error(packet.id, Component.text("Could not find a Rail at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ()));
            return;
        }

        // Couldn't find spawn-location
        SpawnableGroup.SpawnLocationList spawnLocations = TCHelper.getSpawnLocations(spawnable, rail, portal.getSign());
        if (spawnLocations == null) {
            Util.debug("Could not find the right spot to spawn a train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
            // TODO: Inform players
            Passenger.error(packet.id, Component.text("Could not find the right spot to spawn a train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ()));
            return;
        }

        // Load the chunks first
        TCHelper.loadChunks(spawnLocations, 2, 5);

        // Check that the area isn't occupied by another train
        if (spawnLocations.isOccupied()) {
            Util.debug("Track is occupied by another train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
            // TODO: Inform players
            Passenger.error(packet.id, Component.text("Track is occupied by another train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ()));
            return;
        }

        // Spawn
        MinecartGroup group = spawnable.spawn(spawnLocations);

        // Rename
        String newName = packet.oldName;
        if (TrainProperties.get(newName) != null)
            newName = TrainProperties.generateTrainName(newName + "-#");
        group.getProperties().setTrainName(newName);

        Util.debug("Train spawned! Name: " + group.getProperties().getTrainName() + " OldName: " + packet.oldName + " Size: " + group.size());

        // Tell passengers the new train name
        Passenger.setTrainName(packet.id, group.getProperties().getTrainName());

        // Cache received trains
        for (String trainName : receivedTrains.keySet()) {
            if (TrainProperties.get(trainName) == null) continue;
            receivedTrains.remove(trainName);
        }
        receivedTrains.put(group.getProperties().getTrainName(), portal);

        // Route fix
        if (group.getProperties().getDestination().equals(CraftBahnPlugin.plugin.getServerName()) && group.getProperties().getNextDestinationOnRoute() != null)
            group.getProperties().setDestination(group.getProperties().getNextDestinationOnRoute());

        // Launch
        long launchDelayTicks = plugin.getConfig().getLong("Portals.LaunchDelayTicks");
        double launchSpeed = plugin.getConfig().getDouble("Portals.LaunchSpeed");
        double launchDistance = plugin.getConfig().getDouble("Portals.LaunchDistanceBlocks");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Vector headDirection = spawnLocations.locations.get(spawnLocations.locations.size() - 1).forward;
            BlockFace launchDirection = com.bergerkiller.bukkit.tc.Util.vecToFace(headDirection, false);
            group.head().getActions().addActionLaunch(launchDirection, launchDistance, launchSpeed);
            Util.debug("launchDirection: " + launchDirection.name() + " delay: " + launchDelayTicks + " distance: " + launchDistance + " speed: " + launchSpeed);
        }, launchDelayTicks);

        Util.debug(group.getProperties().getTrainName() + " came from " + packet.sourceServer + " (" + packet.portalName + ") to " + plugin.getServerName() + " (" + portal.getSign().getLine(1) + " - " + portal.getSign().getLine(2) + ")");
    }

    public void sendEntityToServer(LivingEntity entity, Portal portal) {
        EntityHandle entityHandle = EntityHandle.fromBukkit(entity);
        CommonTagCompound tagCompound = new CommonTagCompound();
        entityHandle.saveToNBT(tagCompound);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TCPClient client = new TCPClient(portal.getTargetHost(), portal.getTargetPort());
            client.sendAuth(plugin.getConfig().getString("Portals.Server.SecretKey"));
            client.send(new EntityPacket(entity.getUniqueId(), entity.getType()));

            try {
                tagCompound.writeToStream(client.getOutputStream(), false);
                Util.debug("entity (" + entity.getType() + ") was sent to " + portal.getTargetLocation().getServer());
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                client.disconnect();
            }
        });
    }

    @EventHandler
    public void receiveEntity(EntityReceivedEvent event) {
        Util.debug("Received entity (" + event.getType() + ") from " + event.getSourceServer());

        Passenger passenger = Passenger.get(event.getUuid());
        MinecartGroup group = TCHelper.getTrain(passenger.getTrainName());

        if (group == null || group.get(passenger.getCartIndex()) == null) {
            Util.debug("Unable to get spawnLocation for entity (" + event.getType() + ")");
            return;
        }

        // Spawn Entity
        Location location = group.get(passenger.getCartIndex()).getEntity().getLocation();

        location.getWorld().spawn(location, event.getType().getEntityClass(), CreatureSpawnEvent.SpawnReason.CUSTOM, spawnedEntity -> {
            spawnedEntity.setInvulnerable(true);

            // Load received NBT to spawned Entity
            EntityHandle entityHandle = EntityHandle.fromBukkit(spawnedEntity);
            entityHandle.loadFromNBT(event.getTagCompound());
        });
    }

    public void sendPlayerToServer(Player player, Portal portal) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(portal.getTargetLocation().getServer());
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    // Handle spawned entity if it was a passenger
    public static void reEnterEntity(Passenger passenger, CreatureSpawnEvent event) {
        event.setCancelled(false);

        if (passenger.hasError()) {
            Passenger.remove(passenger.getUUID());
            return;
        }

        LivingEntity entity = event.getEntity();
        MinecartGroup group = TCHelper.getTrain(passenger.getTrainName());

        if (group == null) {
            Util.debug("Could not find train (" + passenger.getTrainName() + ") for entity " + entity.getType());
            Passenger.remove(passenger.getUUID());
            return;
        }

        MinecartMember<?> member = group.get(passenger.getCartIndex());

        if (member instanceof MinecartMemberRideable) {
            entity.teleport(member.getEntity().getLocation());
            member.getEntity().getEntity().addPassenger(entity);
            entity.setInvulnerable(false);

            Passenger.remove(passenger.getUUID());
            Util.debug("Passenger (" + entity.getType() + ") " + entity.getUniqueId() + " sucessfully reEntered");
        }
        else
            Util.debug("Unable to put entity " + entity.getType() + " back on train (" + group.getProperties().getTrainName() + ") the cart (" + passenger.getCartIndex() + ") is not rideable");

        Passenger.remove(passenger.getUUID());
    }

    // Handle joined player if he was a passenger
    public static void reEnterPlayer(Passenger passenger, PlayerSpawnLocationEvent event) {
        // Check if some error occurred
        if (passenger.hasError()) {
            Bukkit.getScheduler().runTaskLater(CraftBahnPlugin.plugin, () -> {
                event.getPlayer().sendMessage(passenger.getError());
                Passenger.remove(passenger.getUUID());
            }, 20L);

            return;
        }

        Player player = event.getPlayer();
        MinecartGroup group = TCHelper.getTrain(passenger.getTrainName());

        if (group == null) {
            // TODO: Inform player
            Util.debug("Could not find train (" + passenger.getTrainName() + ") for player " + player.getName());
            Passenger.remove(passenger.getUUID());
            return;
        }

        MinecartMember<?> member = group.get(passenger.getCartIndex());

        if (member instanceof MinecartMemberRideable) {
            event.setSpawnLocation(member.getEntity().getLocation());

            if (player.isFlying())
                player.setFlying(false);

            member.getEntity().getEntity().addPassenger(player);
            Passenger.remove(passenger.getUUID());
        }
        else
            Util.debug("Unable to put player " + player.getName() + " back on train (" + group.getProperties().getTrainName() + ") the cart (" + passenger.getCartIndex() + ") is not rideable");

        Passenger.remove(passenger.getUUID());
        Util.debug("Passenger (" + player.getName() + ") sucessfully reEntered");
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

    public Map<String, Portal> getPendingTeleports() {
        return pendingTeleports;
    }
}
