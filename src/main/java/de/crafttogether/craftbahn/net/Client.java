package de.crafttogether.craftbahn.net;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class Client extends Thread {
    private static final Collection<Client> activeClients = new ArrayList<>();

    private Socket clientSocket;
    private OutputStream outputStream;

    public Client(int port) {

        try {
            clientSocket = new Socket("localhost", port);
            outputStream = clientSocket.getOutputStream();
        } catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                CraftBahnPlugin.getInstance().getLogger().warning("Couldn't connect to server at 127.0.0.1:" + port);
                Message.debug("Error: " + e.getMessage());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (isConnected()) {
            Client.activeClients.add(this);
            Message.debug("Successfully connected to 127.0.0.1:" + port);
        }
    }

    @Override
    public void run() {

    }

    public Boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public void send(String output) {
        if (!isConnected())
            return;

        try {
            PrintWriter pw = new PrintWriter(outputStream);
            pw.write(output);
            pw.flush();

            //Message.debug("Sent:");
            //Message.debug(output);
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

    public static Collection<Client> getActiveClients() {
        return Client.activeClients;
    }

    public static void closeAll() {
        int stopped = 0;

        for (Client client : getActiveClients()) {
            client.disconnect();
            stopped++;
        }

        Message.debug("Stopped " + stopped + " active clients.");
    }
}
