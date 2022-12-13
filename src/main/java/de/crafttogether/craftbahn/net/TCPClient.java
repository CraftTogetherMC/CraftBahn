package de.crafttogether.craftbahn.net;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.net.events.EntityReceivedEvent;
import de.crafttogether.craftbahn.net.events.PacketReceivedEvent;
import de.crafttogether.craftbahn.net.packets.*;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

public class TCPClient extends Thread {
    public static final Collection<TCPClient> activeClients = new ArrayList<>();

    private Socket connection;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream objOutputStream;
    private ObjectInputStream objInputStream;

    public TCPClient(Socket connection) {
        this.connection = connection;

        try {
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connection.isConnected() && objOutputStream != null)
            read();
    }

    public TCPClient(String host, int port) {
        try {
            connection = new Socket(host, port);
            outputStream = connection.getOutputStream();
            inputStream = connection.getInputStream();
            objOutputStream = new ObjectOutputStream(outputStream);
            objInputStream = new ObjectInputStream(inputStream);
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

        if (connection != null && connection.isConnected() && objOutputStream != null) {
            Util.debug("[TCPClient]: Successfully connected to " + host + ":" + port, false);
            start();
        }
    }

    @Override
    public void run() {
        read();
    }

    public void read() {
        activeClients.add(this);

        try {
            Object inputPacket;
            boolean authenticated = false;

            while ((inputPacket = objInputStream.readObject()) != null) {
                Util.debug("[TCPClient]: Packet received: " + inputPacket.getClass().getTypeName() + " " + inputPacket.getClass().getName());

                // First packet has to be our secretKey
                if (!authenticated && inputPacket instanceof AuthenticationPacket packet) {
                    if (packet.key.equals(CraftBahnPlugin.plugin.getConfig().getString("Portals.Server.SecretKey")))
                        authenticated = true;

                    else {
                        Util.debug("[TCPClient]: " + getAddress() + " has sent an invalid authentication");
                        send("error:invalid authentication");
                        disconnect();
                    }
                }

                else if (authenticated) {
                    if (inputPacket instanceof EntityPacket packet) {
                        Event event = new EntityReceivedEvent(packet.uuid, packet.type, CraftBahnPlugin.plugin.getServerName(), CommonTagCompound.readFromStream(inputStream));
                        Bukkit.getServer().getScheduler().runTask(CraftBahnPlugin.plugin, () -> CommonUtil.callEvent(event));
                    }

                    else {
                        Util.debug(inputPacket.getClass().getName());
                        Event event = new PacketReceivedEvent(connection, (Packet) inputPacket);
                        Bukkit.getServer().getScheduler().runTask(CraftBahnPlugin.plugin, () -> CommonUtil.callEvent(event));
                    }
                }

                else {
                    Util.debug("[TCPClient]: " + getAddress() + " is not authenticated.");
                    send("error:authetication failed");
                    disconnect();
                }
            }
        }

        catch (EOFException ignored) { Util.debug("end of stream"); }

        catch (SocketException ex) {
            if ("Socket closed".equals(ex.getMessage())) {
                Util.debug("[TCPClient]: Connection to " + connection.getInetAddress().getHostAddress() + " was closed.", false);
            } else {
                Util.debug("[TCPClient]: " + ex.getMessage());
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

    public boolean send(Packet packet) {
        if (connection == null || !connection.isConnected() || connection.isClosed())
            return false;

        try {
            objOutputStream.reset();
            objOutputStream.writeObject(packet);
            objOutputStream.flush();

            Util.debug("[TCPClient]: Packet was sent: " + packet.getClass().getTypeName() + " " + packet.getClass().getName());
        }
        catch (SocketException e) {
            Util.debug(e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean send(String message) {
        return send(new MessagePacket(message));
    }

    public void disconnect() {
        try {
            if (objInputStream != null)
                objInputStream.close();

            if (objOutputStream != null)
                objOutputStream.close();

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
        AuthenticationPacket packet = new AuthenticationPacket(CraftBahnPlugin.plugin.getServerName(), secretKey);
        send(packet);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}