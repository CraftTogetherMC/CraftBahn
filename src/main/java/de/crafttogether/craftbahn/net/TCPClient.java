package de.crafttogether.craftbahn.net;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.net.events.PacketReceivedEvent;
import de.crafttogether.craftbahn.net.packets.AuthenticationPacket;
import de.crafttogether.craftbahn.net.packets.MessagePacket;
import de.crafttogether.craftbahn.net.packets.Packet;
import de.crafttogether.craftbahn.net.packets.TrainPacket;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

public class TCPClient extends Thread {
    public static final Collection<TCPClient> activeClients = new ArrayList<>();

    private Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public TCPClient(Socket connection) {
        this.connection = connection;
        activeClients.add(this);

        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            inputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        start();
    }

    public TCPClient(String host, int port) {
        activeClients.add(this);

        try {
            connection = new Socket(host, port);
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            inputStream = new ObjectInputStream(connection.getInputStream());
        }
        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CraftBahnPlugin.plugin.getLogger().warning("Couldn't connect to server at " + host + ":" + port);
                Util.debug("[TCPClient]: Error: " + e.getMessage(), false);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (connection != null && connection.isConnected()) {
            Util.debug("[TCPClient]: Successfully connected to " + host + ":" + port, false);
            listen();
        }
    }

    @Override
    public void run() {
        listen();
    }

    public void listen() {
        if (connection == null || !connection.isConnected() || connection.isClosed()) {
            Util.debug("read() not connected -> abort");
            return;
        }

        try {
            Object inputPacket;
            boolean authenticated = false;

            while ((inputPacket = inputStream.readObject()) != null) {
                Util.debug("Packet received: " + inputPacket.getClass().getTypeName() + " " + inputPacket.getClass().getName());

                // First packet has to be our secretKey
                if (!authenticated && inputPacket instanceof AuthenticationPacket packet) {
                    if (packet.key.equals(CraftBahnPlugin.plugin.getConfig().getString("Portals.Server.SecretKey"))) {
                        authenticated = true;
                        continue;
                    }
                    else {
                        Util.debug("invalid authentication");
                        send("invalid authentication");
                        //disconnect();
                    }
                }

                else if (authenticated) {
                    Event event = new PacketReceivedEvent(connection, (Packet) inputPacket);
                    Bukkit.getServer().getScheduler().runTask(CraftBahnPlugin.plugin, () -> CommonUtil.callEvent(event));
                }

                Util.debug("authetication failed");
                send("authetication failed");
                //disconnect();
            }
        }

        catch (EOFException ignored) { Util.debug("end of stream"); }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                Util.debug("[TCPClient]: Connection to " + connection.getInetAddress().getHostAddress() + " was closed.", false);
            } else {
                Util.debug("[TCPClient]" + ex.getMessage());
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            Util.debug("[TCPClient]: Closing connection to " + connection.getInetAddress().getHostAddress(), false);
            disconnect();
        }
    }



    public void send(Packet packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed()) {
            Util.debug("send() not connected -> abort");
            return;
        }

        try {
            outputStream.reset();
            outputStream.writeObject(packet);
            outputStream.flush();

            if (packet instanceof TrainPacket train)
                Util.debug(train.name + " was successfully sent to " + train.target.getServer() + "!");
            else
                Util.debug("Packet sent: " + packet.getClass().getTypeName() + " " + packet.getClass().getName());

        } catch (SocketException e) {
            Util.debug(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        send(new MessagePacket(message));
    }

    public void disconnect() {
        try {
            if (inputStream != null)
                inputStream.close();

            if (outputStream != null)
                outputStream.close();

            if (connection != null && !connection.isClosed())
                connection.close();

            activeClients.remove(this);
        } catch (Exception ex) {
            Util.debug(ex.getMessage());
        }
    }

    public String getAddress() {
        return this.connection.getInetAddress().getHostAddress();
    }

    public static Collection<TCPClient> getActiveClients() {
        return activeClients;
    }

    public static void closeAll() {
        int stopped = 0;

        for (TCPClient client : getActiveClients()) {
            client.disconnect();
            stopped++;
        }

        Util.debug("[TCPClient]: Stopped " + stopped + " active clients.", false);
    }

    public void sendAuth(String secretKey) {
        AuthenticationPacket packet = new AuthenticationPacket();
        packet.server = CraftBahnPlugin.plugin.getServerName();
        packet.key = secretKey;
        send(packet);
    }
}