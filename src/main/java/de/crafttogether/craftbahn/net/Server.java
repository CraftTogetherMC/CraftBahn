package de.crafttogether.craftbahn.net;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

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
                Message.debug(ip + " connected.");

                try {
                    inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder received = new StringBuilder();

                    String inputLine;
                    while ((inputLine = reader.readLine()) != null)
                        received.append(inputLine + "\r\n");

                    Message.debug("Received:");
                    Message.debug(received.toString());

                    // Deserialize received Data
                    ConfigurationNode data = new ConfigurationNode();
                    data.loadFromString(received.toString()); // Deserialize received ConfigurationNode

                    // Process received information
                    PortalHandler.receiveTrain(data);
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