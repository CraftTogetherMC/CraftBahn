package de.crafttogether.craftbahn.net;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class TCPClient extends Thread {
    private static final Collection<TCPClient> activeClients = new ArrayList<>();

    private final String host;
    private final int port;
    private Socket clientSocket;
    private OutputStream outputStream;

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.start();
    }

    @Override
    public void run() {
        try {
            clientSocket = new Socket(host, port);
            outputStream = clientSocket.getOutputStream();
        } catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CraftBahnPlugin.plugin.getLogger().warning("Couldn't connect to server at " + host + ":" + port);
                Util.debug("Error: " + e.getMessage(), false);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (isConnected()) {
            TCPClient.activeClients.add(this);
            Util.debug("Successfully connected to 127.0.0.1:" + port, false);
        }
    }

    public Boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public void send(String message) {
        if (!isConnected())
            return;

        try {
            PrintWriter pw = new PrintWriter(outputStream);
            pw.write(message);
            pw.flush();

            Util.debug("Sent:", false);
            Util.debug(message, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }

            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();

            activeClients.remove(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Collection<TCPClient> getActiveClients() {
        return TCPClient.activeClients;
    }

    public static void closeAll() {
        int stopped = 0;

        for (TCPClient client : getActiveClients()) {
            client.disconnect();
            stopped++;
        }

        Util.debug("Stopped " + stopped + " active clients.", false);
    }
}