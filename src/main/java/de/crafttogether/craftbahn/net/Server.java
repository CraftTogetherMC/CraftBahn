package de.crafttogether.craftbahn.net;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.generated.net.minecraft.world.entity.EntityHandle;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Server extends Thread {
    private int port;
    private ServerSocket serverSocket;
    private boolean listen;
    private ArrayList<Socket> clients;

    public static class MessageReceiveEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();

        String sender;
        String message;

        public MessageReceiveEvent(String sender, String message, boolean isAsynchronous) {
            super(isAsynchronous);
            this.sender = sender;
            this.message = message;
        }

        public String getSender() { return sender; }
        public String getMessage() { return message; }

        @NotNull
        @Override
        public HandlerList getHandlers() { return HANDLERS; }
        public static HandlerList getHandlerList() { return HANDLERS; }
    }

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

                String senderAddress = connection.getInetAddress().getHostAddress();
                Message.debug(senderAddress + " connected.");

                try {
                    inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder received = new StringBuilder();

                    String inputLine;

                    while ((inputLine = reader.readLine()) != null) {

                        if (inputLine.startsWith("entity")) {
                            String[] entityInfo = inputLine.split(";");
                            if (!entityInfo[0].equals("entity")) continue;

                            UUID uuid = UUID.fromString(entityInfo[1]);
                            EntityType entityType = EntityType.valueOf(entityInfo[2]);

                            Message.debug("ENTITY RECEIVED BRUH!!! (" + entityType + ") <33");
                            CommonTagCompound tagCompound = CommonTagCompound.readFromStream(inputStream);
                            Bukkit.getScheduler().runTask(CraftBahnPlugin.getInstance(), () -> PortalHandler.receiveEntity(uuid, entityType, tagCompound));
                            return;
                        }

                        received.append(inputLine).append("\r\n");
                    }


                    //Message.debug("Received:");
                    //Message.debug(received.toString());

                    ConfigurationNode dataPacket = new ConfigurationNode();
                    dataPacket.loadFromString(received.toString());
                    String type = dataPacket.get("type", String.class);

                    if ("trainData".equals(type))
                        Bukkit.getScheduler().runTask(CraftBahnPlugin.getInstance(), () -> PortalHandler.receiveTrain(dataPacket.getNode("body")));
                }

                catch (Exception ex) {
                    ex.printStackTrace();
                }

                finally {
                    Message.debug("Closing connection. (" + Arrays.toString(connection.getInetAddress().getAddress()) + ")");

                    assert reader != null;
                    reader.close();
                    inputStream.close();

                    clients.remove(connection);
                    connection.close();
                }
            }

        } catch (BindException e) {
            Message.debug("Can't bind to " + port + ".. Port already in use!");
            CraftBahnPlugin.getInstance().onDisable();

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