package de.crafttogether.craftbahn.net;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.event.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class TCPClient extends Thread {
    public static final Collection<TCPClient> activeClients = new ArrayList<>();

    private Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private boolean isActive;

    public TCPClient(Socket connection) {
        this.connection = connection;
        isActive = true;
        activeClients.add(this);
        start();
    }

    public TCPClient(String host, int port) {
        try {
            connection = new Socket(host, port);
        }

        catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CraftBahnPlugin.plugin.getLogger().warning("Couldn't connect to server at " + host + ":" + port);
                Util.debug("[TCPClient]: Error: " + e.getMessage(), false);
            }
        }

        catch (IOException ex) {
            ex.printStackTrace();
        }

        Util.debug("[TCPClient]: Successfully connected to " + host + ":" + port, false);

        isActive = true;
        activeClients.add(this);
        start();
    }

    @Override
    public void run() {
        BufferedReader reader = null;

        try {
            Object input;
            inputStream = (ObjectInputStream) connection.getInputStream();
            outputStream = (ObjectOutputStream) connection.getOutputStream();

            while (isActive && (input = inputStream.readObject()) != null) {
                Packet receivedPacket = (Packet) input;

                Util.debug("[TCPClient]: Received:", false);
                Util.debug(receivedPacket.message, false);

                Event event = new PacketReceivedEvent(connection, receivedPacket.message);
                CommonUtil.callEvent(event);
            }
        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            Util.debug("[TCPClient]: Closing connection to (" + Arrays.toString(connection.getInetAddress().getAddress()) + ")", false);
            disconnect();
        }
    }

    public Boolean isConnected() {
        return connection != null && connection.isConnected() && !connection.isClosed();
    }

    public void send(String message) {
        if (!isConnected()) return;

        try {
            outputStream.writeObject(new Packet(message));
            Util.debug("[TCPClient]: Sent:", false);
            Util.debug(message, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        if (!isActive) return;

        try {
            if (inputStream != null)
                inputStream.close();

            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }

            if (connection != null && !connection.isClosed())
                connection.close();

            isActive = false;
            activeClients.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
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
}