package de.crafttogether.craftbahn.net;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class TCPServer extends Thread {
    private final String host;
    private final int port;
    private boolean listen;
    private ServerSocket serverSocket;
    private ArrayList<TCPClient> clients;

    public TCPServer(String host, int port) {
        this.host = host;
        this.port = port;
        start();
    }

    @Override
    public void run() {
        clients = new ArrayList<>();

        try {
            // Create ServerSocket
            serverSocket = new ServerSocket(port, 5, InetAddress.getByName(host));
            listen = true;

            Util.debug("[TCPServer]: Server is listening on port " + port, false);

            // Handle incoming connections
            while (listen && !isInterrupted()) {
                Socket connection = null;

                try {
                    connection = serverSocket.accept();
                } catch (SocketException e) {
                    Util.debug(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (connection == null)
                    continue;

                TCPClient client = new TCPClient(connection);
                Util.debug("[TCPServer]: " + client.getAddress() + " connected.", false);

                // Should we accept remote connections?
                boolean acceptRemote = CraftBahnPlugin.plugin.getConfig().getBoolean("Portals.Server.AcceptRemoteConnections");
                if (!acceptRemote && !client.getAddress().equals("127.0.0.1")) {
                    Util.debug("[TCPServer]: " + client.getAddress() + " was kicked. (No remote connections allowed)", false);
                    client.send("Remote connections are not allowed");
                    client.disconnect();
                }

                clients.add(client);
            }

        } catch (BindException e) {
            CraftBahnPlugin.plugin.getLogger().warning("[TCPServer]: Can't bind to " + port + ".. Port already in use!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();
            Util.debug("[TCPServer]: Server stopped.", false);
        }
    }

    public void close() {
        if (!listen) return;
        listen = false;

        for (TCPClient client : clients)
            client.disconnect();

        if (serverSocket != null) {
            try { serverSocket.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}