package de.crafttogether.craftbahn.net;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.event.Event;

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

public class TCPServer extends Thread {
    private int port;
    private boolean listen;
    private ServerSocket serverSocket;
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

            Util.debug("Server is listening on port " + port, false);

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
                Util.debug(ip + " connected.", false);

                try {
                    inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder received = new StringBuilder();

                    String input;
                    while ((input = reader.readLine()) != null)
                        received.append(input).append("\r\n");

                    //Util.debug("Received:", false);
                    //Util.debug(received.toString(), false);

                    Event event = new PacketReceivedEvent(connection, received.toString());
                    CommonUtil.callEvent(event);
                }

                catch (Exception ex) {
                    ex.printStackTrace();
                }

                finally {
                    Util.debug("Closing connection. (" + Arrays.toString(connection.getInetAddress().getAddress()) + ")", false);

                    if (reader != null)
                        reader.close();

                    if (inputStream != null)
                        inputStream.close();

                    clients.remove(connection);
                    connection.close();
                }
            }

        } catch (BindException e) {
            Util.debug("Can't bind to " + port + ".. Port already in use!", false);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        finally {
            close();
            Util.debug("Server stopped.", false);
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