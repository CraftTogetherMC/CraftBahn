package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.entity.CommonEntity;
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
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.net.Client;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;

public class PortalHandler {

    public static void transmitTrain(MinecartGroup group, Portal portal) {
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
        String trainName = CraftBahnPlugin.getInstance().getServerName() + "-" + group.getProperties().getTrainName();

        // Get train-owners
        Set<String> owners = group.getProperties().getOwners();

        // Get passengers
        List<Player> playerPassengers = new ArrayList<>();
        //List<String> mobPassengers = new ArrayList<>();
        List<String> passengerList = new ArrayList<>();

        /* Save & Load Entity NBT
        Entity entity = member.getEntity().getEntity();
        EntityHandle entityHandle = EntityHandle.fromBukkit(entity);

        CommonTagCompound tagCompound = CommonTagCompound.create(entityHandle);
        entityHandle.saveToNBT(tagCompound);

        entityHandle.loadFromNBT(tagCompound);
        */

        for (MinecartMember<?> member : group) {
            List<Player> cartPassengers = TCHelper.getPlayerPassengers(member);

            for (Player passenger : cartPassengers) {
                playerPassengers.add(passenger);
                passengerList.add(passenger.getUniqueId() + ";" + trainID + ";" + member.getIndex());
            }
        }

        // Use ConfigurationNode to store informations
        ConfigurationNode dataPacket = new ConfigurationNode();
        dataPacket.set("target.world", portal.getTargetLocation().getWorld());
        dataPacket.set("target.x", portal.getTargetLocation().getX());
        dataPacket.set("target.y", portal.getTargetLocation().getY());
        dataPacket.set("target.z", portal.getTargetLocation().getZ());
        dataPacket.set("train.id", trainID);
        dataPacket.set("train.newName", trainName);
        dataPacket.set("train.owners", owners);
        dataPacket.set("train.properties", trainProperties);
        dataPacket.set("train.passengers", passengerList);

        // Send dataPacket to server
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
        String trainID = (String) trainData.get("train.id");
        String trainNewName = (String) trainData.get("train.newName");
        List<Object> owners = trainData.getList("train.owners");
        SpawnableGroup train = SpawnableGroup.fromConfig(trainData.getNode("train.properties"));
        List<Object> passengers = trainData.getList("train.passengers");

        // Add players to passengerQueue
        for (Object passengerData : passengers) {
            String[] passenger = ((String) passengerData).split(";");
            Passenger.register(UUID.fromString(passenger[0]), passenger[1], Integer.parseInt(passenger[2]));
        }

        // Check if world exists
        if (world == null) {
            Passenger.sendMessage(trainID, "§cWorld '" + worldName + "' was not found!", 2);
            return;
        }

        // Use scheduler to be sync with main-thread
        Bukkit.getServer().getScheduler().runTask(CraftBahnPlugin.getInstance(), () -> {
            
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

            List<Location> spawnLocations = SignActionSpawn.getSpawnPositions(railLoc, false, facing, train.getMembers());

            // load chunks
            for(Location spawnLoc : spawnLocations)
                spawnLoc.getChunk().load();

            // Spawn train
            MinecartGroup spawnedTrain = MinecartGroup.spawn(train, spawnLocations);
            TrainProperties trainProperties = spawnedTrain.getProperties();

            // Clear Inventory if needed
            if (sign.getLine(3).equalsIgnoreCase("clear"))
                TCHelper.clearInventory(spawnedTrain);

            // Set properties
            spawnedTrain.getProperties().setTrainName(trainID);

            Set<String> ownerSet = new HashSet<>();
            for (Object owner : owners)
                ownerSet.add(Objects.toString(owner, null));

            for (CartProperties cartProp : spawnedTrain.getProperties())
                cartProp.setOwners(ownerSet);

            // Launch train
            double launchSpeed = (trainProperties.getSpeedLimit() > 0) ? trainProperties.getSpeedLimit() : 0.4;
            spawnedTrain.head().getActions().addActionLaunch(facing, LauncherConfig.parse("10b"), launchSpeed);
        });
    }

    // Handle joined player if he was a passenger
    public static void reEnterPassenger(Passenger passenger, PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String trainId = passenger.getTrainId();
        int cartIndex = passenger.getCartIndex();

        // Try to find train and set player as passenger
        MinecartGroup train = TCHelper.getTrain(trainId);

        if (train == null)
            return;

        MinecartMember<?> cart = train.get(cartIndex);

        if (cart instanceof MinecartMemberRideable) {
            if (player.isFlying())
                player.setFlying(false);

            //e.setSpawnLocation(cart.getEntity().getLocation());
            player.teleport(cart.getEntity().getLocation());
            cart.getEntity().setPassenger(player);
            Passenger.remove(passenger.getUUID());
        }
    }

    public static void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(CraftBahnPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }
}
