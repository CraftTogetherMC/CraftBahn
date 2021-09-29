package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.common.nbt.CommonTag;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.nbt.CommonTagList;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.generated.net.minecraft.world.entity.EntityHandle;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.net.Client;
import de.crafttogether.craftbahn.net.Server;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class PortalHandler {
    private static List<String> spawnedTrains = new ArrayList<>();

    public static void transmitTrain(MinecartGroup group, Portal portal) {
        // Save train and get properties
        ConfigurationNode trainProperties = group.saveConfig();

        // Remove lastPathNode from cartProperties
        Set<ConfigurationNode> nodes = trainProperties.getNode("carts").getNodes();
        for (ConfigurationNode cart : nodes) {
            if (cart.contains("lastPathNode"))
                cart.set("lastPathNode", "");
        }

        // Generate unique id and new name
        String trainID = UUID.randomUUID().toString().split("-")[0];
        String trainNewName = CraftBahnPlugin.getInstance().getServerName() + "-" + group.getProperties().getTrainName();

        // Get train-owners
        Set<String> owners = group.getProperties().getOwners();

        // Get passengers
        List<ConfigurationNode> passengers = new ArrayList<>();

        for (MinecartMember<?> member : group) {
            Message.debug(member.getEntity().getType().getName() + " (" + member.getEntity().getUniqueId() + ")");

            for (Entity passenger : member.getEntity().getEntity().getPassengers()) {
                String type = null;

                if (passenger instanceof Player)
                    type = "Player";

                else if (passenger instanceof LivingEntity)
                    type = "LivingEntity";

                if (type != null) {
                    ConfigurationNode passengerDetails = new ConfigurationNode();
                    passengerDetails.set("uuid", passenger.getUniqueId().toString());
                    passengerDetails.set("type", type);
                    passengerDetails.set("cartIndex", member.getIndex());
                    passengers.add(passengerDetails);
                }
            }
        }

        // Use ConfigurationNode to store informations
        ConfigurationNode trainData = new ConfigurationNode();
        trainData.set("target.world", portal.getTargetLocation().getWorld());
        trainData.set("target.x", portal.getTargetLocation().getX());
        trainData.set("target.y", portal.getTargetLocation().getY());
        trainData.set("target.z", portal.getTargetLocation().getZ());
        trainData.set("train.id", trainID);
        trainData.set("train.name", group.getProperties().getTrainName());
        trainData.set("train.newName", trainNewName);
        trainData.set("train.owners", owners);
        trainData.set("train.properties", trainProperties);
        trainData.set("train.passengers", passengers);

        // Send dataPacket to server
        ConfigurationNode dataPacket = new ConfigurationNode();
        dataPacket.set("type", "trainData");
        dataPacket.set("body", trainData);

        Client client = new Client(portal.getTargetPort());
        client.send(dataPacket.toString()); // Serialize ConfigurationNode
        client.disconnect();
    }

    public static void receiveTrain(ConfigurationNode trainData) {
        String worldName = ((String) trainData.get("target.world"));
        World world = Bukkit.getWorld(worldName);

        double x = (double) trainData.get("target.x");
        double y = (double) trainData.get("target.y");
        double z = (double) trainData.get("target.z");

        Location targetLocation = new Location(world, x, y, z);
        String trainID = trainData.get("train.id", String.class);
        String trainName = trainData.get("train.name", String.class);
        String trainNewName = trainData.get("train.newName", String.class);
        Set<String> owners = Set.copyOf(trainData.getList("train.owners", String.class));
        ConfigurationNode trainConfig = trainData.getNode("train.properties");
        List<ConfigurationNode> passengers = trainData.getList("train.passengers", ConfigurationNode.class);

        // Process passengers
        for (ConfigurationNode passengerDetails : passengers) {
            Message.debug("Passenger received");
            String type = passengerDetails.get("type", String.class);

            // Skip non-player-passengers
            if (!type.equals("Player") && !type.equals("LivingEntity")) { Message.debug("Invalid type"); continue;}

            UUID uuid = UUID.fromString(passengerDetails.get("uuid", String.class));
            Integer cartIndex = passengerDetails.get("cartIndex", Integer.class);

            Message.debug("Register passenger " + uuid + " " + trainID);
            Passenger.register(uuid, trainID, cartIndex);
        }

        // Check if world exists
        if (world == null) {
            Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        // Load train from received config
        SpawnableGroup train = SpawnableGroup.fromConfig(trainConfig);

        /* Look for "portal-out"-sign */
        Block signBlock = targetLocation.getBlock();
        Sign sign;
        BlockData blockData;
        BlockFace facing;

        if (signBlock.getState() instanceof Sign) {
            sign = (Sign) signBlock.getState();
            blockData = signBlock.getState().getBlockData();

            if (blockData instanceof Rotatable)
                facing = ((Rotatable) blockData).getRotation();

            else {
                Passenger.sendMessage(trainID, "§cSign not placed underneath the rails", 3);
                return;
            }
        } else {
            Passenger.sendMessage(trainID, "§cNo Sign found! (" + signBlock.getType().name() + ")", 3);
            return;
        }

        /* Get spawn-rail */
        Location railLoc = signBlock.getLocation();
        railLoc.setY(targetLocation.getY() + 2);
        Block railBlock = railLoc.getBlock();

        if (!(railLoc.getBlock().getBlockData() instanceof Rail)) {
            Passenger.sendMessage(trainID, "§cNo Rail found! (" + railBlock.getType().name() + ")", 3);
            return;
        }

        // Load Chunks
        SpawnableGroup.SpawnLocationList spawnLocations = train.findSpawnLocations(railBlock, facing.getDirection(), SpawnableGroup.SpawnMode.DEFAULT);
        spawnLocations.loadChunks();

        // Spawn train
        Message.debug("Spawn train " + trainNewName + " #" + trainID);
        MinecartGroup spawnedTrain = train.spawn(spawnLocations);

        // Set trainName
        spawnedTrain.getProperties().setTrainName(trainID);
        spawnedTrains.add(trainID);

        // Clear Inventory if needed
        if (sign.getLine(3).equalsIgnoreCase("clear"))
            TCHelper.clearInventory(spawnedTrain);

        // Set owners
        for (CartProperties cartProp : spawnedTrain.getProperties())
            cartProp.setOwners(owners);

        // Stop train
        spawnedTrain.head().stop();

        //Bukkit.getScheduler().runTaskLater(CraftBahnPlugin.getInstance(), () -> {
        //    Message.debug("Launch train after 100 ticks over " + CraftBahnPlugin.getInstance().getConfig().getInt("Portals.LaunchDistanceBlocks") + " blocks at speed: " + launchSpeed);
            spawnedTrain.head().getActions().addActionLaunch(facing, CraftBahnPlugin.getInstance().getConfig().getDouble("Portals.LaunchDistanceBlocks"), CraftBahnPlugin.getInstance().getConfig().getDouble("Portals.LaunchSpeed"));
        //}, 100L);
    }

    public static void receiveEntity(UUID uuid, EntityType entityType, CommonTagCompound entityNBT) {
        Message.debug("Received entity! " + entityType + " " + uuid);
        Passenger passenger = Passenger.get(uuid);
        if (passenger == null) {
        Message.debug("Entity is not a Passenger!"); return; }

        MinecartGroup train = TCHelper.getTrain(passenger.getTrainId());
        Location location = train.head().getLastBlock().getLocation().add(0, 1, 0);

        // Spawn entity
        Message.debug("Spawn entity....");
        Entity spawnedEntity = location.getWorld().spawnEntity(location, entityType);

        // Load received NBT to spawned Entity

        Message.debug("Load NBT....");
        EntityHandle entityHandle = EntityHandle.fromBukkit(spawnedEntity);
        entityHandle.loadFromNBT(entityNBT);

        // Set as passenger
        Message.debug("Set as passenger of train....");
        MinecartMember<?> cart = train.get(passenger.getCartIndex());

        // Add passeneger to cart
        cart.getEntity().setPassenger(spawnedEntity);

        Bukkit.getScheduler().runTaskLater(CraftBahnPlugin.getInstance(), () -> {
            if (!cart.getEntity().hasPassenger()) {
                Message.debug("reEnter " + spawnedEntity.getName() + " (" + passenger.getCartIndex() + ")... Second try.");
                cart.getEntity().setPassenger(spawnedEntity);
            }
        }, 40L);

        // Remove Passenger from list
        Passenger.remove(passenger.getUUID());
    }

    // Handle joined player if he was a passenger
    public static void reEnterPassenger(Passenger passenger, PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String trainId = passenger.getTrainId();
        int cartIndex = passenger.getCartIndex();

        Message.debug(e.getPlayer().getName() + " -> Try to find train #" + trainId);

        // Try to find train and set player as passenger
        MinecartGroup train = PortalHandler.getSpawnedTrain(trainId);

        if (train == null) {
            Message.debug(player.getName() + " -> Train #" + trainId + " not found");
            Message.debug(player, " -> Train #" + trainId + " not found");
            return;
        }

        Message.debug(e.getPlayer().getName() + " -> Try to find a seat at index: " + cartIndex + "/" + train.size());

        MinecartMember<?> cart = train.get(cartIndex);
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 60, 1);

        if (cart instanceof MinecartMemberRideable) {
            if (player.isFlying())
                player.setFlying(false);

            // Add blindness-effect
            player.addPotionEffect(blindness);

            // Add passeneger to cart
            cart.getEntity().setPassenger(player);

            Bukkit.getScheduler().runTaskLater(CraftBahnPlugin.getInstance(), () -> {
                if (!cart.getEntity().hasPassenger()) {
                    Message.debug("reEnter " + player.getName() + "... Second try.");
                    cart.getEntity().setPassenger(player);
                }
            }, 40L);

            // Remove Passenger from list
            Passenger.remove(passenger.getUUID());

            // Remove spawnedTrain from list
            if (Passenger.get(trainId).size() < 1)
                spawnedTrains.remove(trainId);

            // Play Sound
            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 5f, 1f);

            Message.debug(e.getPlayer().getName() + " -> entered #" + trainId);
        }
        else {
            Message.debug(player.getName() + " -> Cart is not ridable");
            Message.debug(player, "Cart is not ridable");
        }
    }

    public static void sendEntityToServer(LivingEntity entity, Portal portal) {
        Client client = new Client(portal.getTargetPort());
        OutputStream outputStream = client.getOutputStream();

        EntityHandle entityHandle = EntityHandle.fromBukkit(entity);
        CommonTagCompound tagCompound = new CommonTagCompound();
        entityHandle.saveToNBT(tagCompound);

        PrintWriter pw = new PrintWriter(outputStream);
        pw.write("entity;" + entity.getUniqueId() + ";" + entity.getType().name() + "\r\n");
        pw.flush();

        Message.debug("Transferring entity (" + entity.getType().name() + ") to " + portal.getTargetLocation().getServer());

        // Transfer entity
        try {
            tagCompound.writeToStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove Entity
        entity.remove();
        client.disconnect();
    }

    public static void sendPlayerToServer(Player player, Portal portal) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(portal.getTargetLocation().getServer());
        player.sendPluginMessage(CraftBahnPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static Collection<String> getSpawnedTrainNames() {
        return spawnedTrains;
    }

    public static MinecartGroup getSpawnedTrain(String id) {
        MinecartGroup train = TCHelper.getTrain(id);
        if (!spawnedTrains.contains(id)) Message.debug("Train '" + id + "' was not spawned by CBPortals");
        if (train == null) Message.debug("Train '" + id + "' was not found");
        return train;
    }
}
