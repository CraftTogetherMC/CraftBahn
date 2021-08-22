package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignActionSpawn;
import com.bergerkiller.bukkit.tc.utils.LauncherConfig;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.net.Client;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;

public class PortalHandler {

    public static void transmitTrain(MinecartGroup group, Portal portal) {
        // Clear Inventory
        // TODO: Make worlds where chest-minecarts should be cleared configurable
        if (group.getWorld().getName().contains("creative"))
            TCHelper.clearInventory(group);

        // Save train and get properties
        ConfigurationNode trainProperties = group.saveConfig();

        // Remove lastPathNode from cartProperties
        Set<ConfigurationNode> nodes = trainProperties.getNode("carts").getNodes();
        for (ConfigurationNode cart : nodes) {
            if (cart.contains("lastPathNode"))
                cart.set("lastPathNode", "");
        }

        // Generate new name
        String trainID = UUID.randomUUID().toString().split("-")[0];
        String trainName = CraftBahn.getInstance().getServerName() + "-" + group.getProperties().getTrainName();

        // Get train-owners
        Set<String> owners = group.getProperties().getOwners();

        // Get passengers
        List<Player> passengers = new ArrayList<>();
        List<String> passengerList = new ArrayList<>();

        for (MinecartMember member : group) {
            List<Player> cartPassengers = TCHelper.getPassengers(member);

            for (Player playerPassenger : cartPassengers) {
                passengers.add(playerPassenger);
                passengerList.add(playerPassenger.getUniqueId() + ";" + trainID + ";" + member.getIndex());
            }
        }

        // Use ConfigurationNode to store informations
        ConfigurationNode dataPacket = new ConfigurationNode();
        dataPacket.set("target.world", portal.getTargetLocation().getWorld());
        dataPacket.set("target.x", portal.getTargetLocation().getX());
        dataPacket.set("target.y", portal.getTargetLocation().getY());
        dataPacket.set("target.z", portal.getTargetLocation().getZ());
        dataPacket.set("train.passengers", passengers);
        dataPacket.set("train.id", trainID);
        dataPacket.set("train.newName", trainName);
        dataPacket.set("train.owners", owners);
        dataPacket.set("train.properties", trainProperties);

        // Send dataPacket to server
        Client client = new Client(portal.getTargetPort());
        client.send(dataPacket.toString()); // Serialize ConfigurationNode
        client.disconnect();
    }

    public static void receiveTrain(ConfigurationNode trainData) {
        String worldName = ((String) trainData.get("target.world"));
        World world = Bukkit.getWorld(worldName);

        int x = (int) trainData.get("target.x");
        int y = (int) trainData.get("target.y");
        int z = (int) trainData.get("target.z");

        Location targetLocation = new Location(world, x, y, z);
        String trainID = (String) trainData.get("train.id");
        String trainNewName = (String) trainData.get("train.newName");
        List<String> passengers = (List<String>) trainData.get("train.passengers");
        List<String> owners = (List<String>) trainData.get("train.owners");
        ConfigurationNode trainProperties = trainData.getNode("train.properties");
        SpawnableGroup train = SpawnableGroup.fromConfig(trainProperties);

        // Add players to passengerQueue
        for (String passengerData : passengers) {
            String[] passenger = passengerData.split(";");
            Passenger.register(UUID.fromString(passenger[0]), passenger[1], Integer.parseInt(passenger[2]));
        }

        // Check if world exists
        if (world == null) {
            Message.debug("World '" + worldName + "' was not found!");
            Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
        }

        // Use scheduler to be sync with main-thread
        Bukkit.getServer().getScheduler().runTask(CraftBahn.getInstance(), () -> {
            
            /* Look for "portal-out"-sign */
            Block signBlock = targetLocation.getBlock();
            BlockFace facing = null;

            if (signBlock.getState() instanceof Sign) {
                BlockData blockData = signBlock.getState().getBlockData();

                if (blockData instanceof Rotatable) {
                    facing = ((Rotatable) blockData).getRotation();
                    Message.debug(facing.getDirection().getBlockX() + " " +
                            facing.getDirection().getBlockX() + " " +
                            facing.getDirection().getBlockX());
                } else {
                    Message.debug("No Rotation found!");
                    Passenger.sendMessage(trainID, "No Rotation found!", 3);
                    return;
                }
            } else {
                Message.debug("No Sign found! (" + signBlock.getType().name() + ")");
                Passenger.sendMessage(trainID, "No Sign found! (" + signBlock.getType().name() + ")", 3);
                return;
            }

            /* Get spawn-rail */
            Location railLoc = signBlock.getLocation();
            railLoc.setY(targetLocation.getY() + 2);
            Block railBlock = railLoc.getBlock();
            Chunk spawnChunk = railLoc.getChunk();

            for (int x1 = (spawnChunk.getX() -5); x1 < (spawnChunk.getX() +5); x1++) {
                for (int z1 = (spawnChunk.getZ() -5); z1 < (spawnChunk.getZ() +5); z1++)
                    railLoc.getWorld().getChunkAt(x1, z1);
            }

            if (!(railLoc.getBlock().getBlockData() instanceof Rail)) {
               Message.debug("No Rail found! (" + railBlock.getType().name() + ")");
                Passenger.sendMessage(trainID, "No Rail found! (" + railBlock.getType().name() + ")", 3);
                return;
            }

            /* DEBUG */
            StringBuilder ownerList = new StringBuilder();
            for (String owner : owners) ownerList.append(owner + ",");

            Message.debug("World: " + world.getName());
            Message.debug("Location: " + x + " " + y + " " + z);
            Message.debug("Direction: " + facing);
            Message.debug("TrainID: " + trainID);
            Message.debug("TrainName: " + trainNewName);
            Message.debug("Owners: " + ownerList.toString());
            Message.debug("Passengers: " + passengers.size());
            Message.debug("Try to spawn a train with " + train.getMembers().size() + " carts...");

            List<Location> spawnLocations = SignActionSpawn.getSpawnPositions(railLoc, false, facing, train.getMembers());

            // load chunks
            for(Location spawnLoc : spawnLocations)
                spawnLoc.getChunk().load();

            // Spawn train
            MinecartGroup spawnedTrain = MinecartGroup.spawn(train, spawnLocations);
            TrainProperties trainProperties1 = spawnedTrain.getProperties();

            // Set properties
            spawnedTrain.getProperties().setTrainName(trainID);
            System.out.println("TRAINNAME: " + trainID);

            Set<String> ownerSet = new HashSet<String>(owners);
            for (CartProperties cartProp : spawnedTrain.getProperties())
                cartProp.setOwners(ownerSet);

            // Launch train
            double launchSpeed = (trainProperties1.getSpeedLimit() > 0) ? trainProperties1.getSpeedLimit() : 0.4;
            spawnedTrain.head().getActions().addActionLaunch(facing, LauncherConfig.parse("10b"), launchSpeed);
        });
    }

    // Handle joined player if he was a passenger
    public static void reEnterPassenger(Passenger passenger, PlayerSpawnLocationEvent e) {
        Player player = Bukkit.getPlayer(passenger.getUUID());

        if (player == null)
            return;

        String trainName = passenger.getTrainName();
        int cartIndex = passenger.getCartIndex();

        Message.debug("Try to find train '" + trainName + "' for " + player.getName() + " cartIndex: " + cartIndex);

        // Try to find train and set player as passenger
        MinecartGroup train = TCHelper.getTrain(trainName);

        if (train != null) {
            MinecartMember<?> cart = train.get(cartIndex);

            if (cart instanceof MinecartMemberRideable) {
                if (player.isFlying())
                    player.setFlying(false);

                e.setSpawnLocation(cart.getBlock().getLocation());
                cart.getEntity().setPassenger(player);

                Message.debug("Set player " + player.getName() + " as passenger of '" + trainName + "' at cartIndex: " + cartIndex);
                Passenger.remove(passenger.getUUID());
            }
            else
                Message.debug("Cart(" + cartIndex + ") at Train '" + trainName + "' is not rideable.");
        }
        else
            Message.debug("Train '" + trainName + "' was not found.");
    }

    public static void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(CraftBahn.getInstance(), "BungeeCord", out.toByteArray());
        Message.debug("Moved player " + player.getName() + " to " + server);
    }
}
