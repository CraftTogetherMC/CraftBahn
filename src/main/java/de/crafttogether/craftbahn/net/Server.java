package de.crafttogether.craftbahn.net;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.properties.CartProperties;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignActionSpawn;
import com.bergerkiller.bukkit.tc.utils.LauncherConfig;
import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.portals.Passenger;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Server extends Thread {
    private int port;
    private ServerSocket serverSocket;
    private boolean listen;
    private ArrayList<Socket> clients;

    public void listen(int port) {
        this.port = port;
        this.start();
    }

    @Override
    public void run() {
        clients = new ArrayList<>();

        try {
            // Create ServerSocket
            serverSocket = new ServerSocket(port);
            listen = true;

            Message.debug("Server is listening on port " + port);

            // Handle incoming connections
            while (listen && !isInterrupted()) {
                Socket connection = null;
                InputStream inputStream = null;
                BufferedReader reader = null;

                try {
                    connection = serverSocket.accept();
                    clients.add(connection);
                } catch (SocketException e) {
                    if (!e.getMessage().equalsIgnoreCase("socket closed"))
                        e.printStackTrace();
                }

                if (connection == null)
                    continue;

                String ip = connection.getInetAddress().getHostAddress();

                // Whitelist Check
                /*if (ICTS.config.isWhitelistEnabled() && !ICTS.config.getIPWhitelist().contains(ip)) {
                    Message.debug(ip + " tried to connect but is not whitelisted!");

                    connection.close();
                    clients.remove(connection);
                    continue;
                }*/

                Message.debug(ip + " connected.");

                try {
                    inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder received = new StringBuilder();

                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        received.append(inputLine + "\r\n");
                    }

                    /*
                    Message.debug("Received:");
                    Message.debug(received.toString());
                    */

                    // Receive dataPacket
                    ConfigurationNode dataPacket = new ConfigurationNode();
                    dataPacket.loadFromString(received.toString()); // Deserialize received ConfigurationNode

                    String worldName = ((String) dataPacket.get("target.world")).replace("ct3creative", "ct3_creative");
                    World world = Bukkit.getWorld(worldName);

                    int x = (int) dataPacket.get("target.x");
                    int y = (int) dataPacket.get("target.y");
                    int z = (int) dataPacket.get("target.z");

                    Location loc = new Location(world, x, y, z);

                    String trainID = (String) dataPacket.get("train.id");
                    String trainNewName = (String) dataPacket.get("train.newName");
                    List<String> passengers = (List<String>) dataPacket.get("train.passengers");
                    List<String> owners = (List<String>) dataPacket.get("train.owners");
                    ConfigurationNode trainProperties = dataPacket.getNode("train.properties");
                    SpawnableGroup train = SpawnableGroup.fromConfig(trainProperties);

                    // Add players to passengerQueue
                    for (String passengerData : passengers) {
                        String[] passenger = passengerData.split(";");
                        Passenger.register(UUID.fromString(passenger[0]), passenger[1], Integer.parseInt(passenger[2]));
                    }

                    if (world == null) {
                        Message.debug("World '" + worldName + "' was not found!");
                        Passenger.sendMessage(trainID, "World '" + worldName + "' was not found!", 2);
                        continue;
                    }

                    // Use scheduler to be sync with main-thread
                    Bukkit.getServer().getScheduler().runTask(CraftBahn.getInstance(), () -> {
                        // Look for "icreceive"-sign
                        Block signBlock = loc.getBlock();
                        BlockFace direction = null;

                        if (signBlock.getState() instanceof Sign) {
                            BlockData blockData = signBlock.getState().getBlockData();

                            if (blockData instanceof Rotatable) {
                                Rotatable rotation = (Rotatable) blockData;
                                direction = rotation.getRotation();
                                CraftBahn.getInstance().getLogger().info(direction.getDirection().getBlockX() + " " +
                                    direction.getDirection().getBlockX() + " " +
                                    direction.getDirection().getBlockX());
                            } else {
                                CraftBahn.getInstance().getLogger().warning("No Rotation found!");
                                return;
                            }
                        } else {
                            Message.debug("No Sign found! (" + signBlock.getType().name() + ")");
                            return;
                        }

                        // Get spawn-rail
                        Location railLoc = signBlock.getLocation();
                        railLoc.setY(loc.getY() + 2);
                        Block railBlock = railLoc.getBlock();
                        Chunk spawnChunk = railLoc.getChunk();

                        for (int x1 = (spawnChunk.getX() -5); x1 < (spawnChunk.getX() +5); x1++) {
                            for (int z1 = (spawnChunk.getZ() -5); z1 < (spawnChunk.getZ() +5); z1++)
                                railLoc.getWorld().getChunkAt(x1, z1);
                        }

                        if (!(railLoc.getBlock().getBlockData() instanceof Rail)) {
                            Message.debug("No Rail found! (" + railBlock.getType().name() + ")");
                            Passenger.sendMessage(trainID, "No Rail found! (" + railBlock.getType().name() + ")");
                            return;
                        }

                        if (CraftBahn.getInstance().getConfig().getBoolean("debug")) {
                            StringBuilder ownerList = new StringBuilder();
                            for (String owner : owners) ownerList.append(owner + ",");

                            Message.debug("World: " + world.getName());
                            Message.debug("Location: " + x + " " + y + " " + z);
                            Message.debug("Direction: " + direction);
                            Message.debug("TrainID: " + trainID);
                            Message.debug("TrainName: " + trainNewName);
                            Message.debug("Owners: " + ownerList.toString());
                            Message.debug("Passengers: " + passengers.size());
                            Message.debug("Try to spawn a train with " + train.getMembers().size() + " carts...");
                        }

                        List<Location> spawnLocations = SignActionSpawn.getSpawnPositions(railLoc, false, direction, train.getMembers());

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
                        spawnedTrain.head().getActions().addActionLaunch(direction, LauncherConfig.parse("10b"), launchSpeed);
                    });
                }

                catch (Exception ex) {
                    ex.printStackTrace();
                }

                finally {
                    Message.debug("Closing connection. (" + connection.getInetAddress().getAddress() + ")");

                    reader.close();
                    inputStream.close();

                    clients.remove(connection);
                    connection.close();
                }
            }

        } catch (BindException e) {
            Message.debug("Can't bind to " + port + ".. Port already in use!");
            CraftBahn.getInstance().onDisable();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();
            Message.debug("Server stopped.");
        }
    }

    public Boolean isReady() {
        return listen;
    }

    public void close() {
        if (!isInterrupted())
            interrupt();

        if (!listen)
            return;

        listen = false;

        for (Socket connection : clients) {
            if (connection.isClosed()) continue;
            try {
                clients.remove(connection);
                connection.close();
            }
            catch (IOException ex) { ex.printStackTrace(); }
        }

        if (serverSocket != null) {
            try { serverSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}