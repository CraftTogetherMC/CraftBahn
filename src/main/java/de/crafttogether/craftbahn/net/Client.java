package de.crafttogether.craftbahn.net;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class Client extends Thread {
    private static Collection<Client> activeClients = new ArrayList<>();

    private Socket clientSocket;
    private OutputStream outputStream;
    private final int port;

    public Client(int port) {
        this.port = port;

        try {
            clientSocket = new Socket("localhost", port);
            outputStream = clientSocket.getOutputStream();
        } catch (ConnectException e) {
            if (!e.getMessage().equalsIgnoreCase("connection refused")) {
                Message.debug("Couldn't connect to server");
                Message.debug("Error: " + e.getMessage());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (isConnected()) {
            Message.debug("ADD ACTIVE CLIENT");
            Client.activeClients.add(this);
        }
    }

    @Override
    public void run() {

    }

    public Boolean isConnected() {
        if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed())
            return true;
        else
            return false;
    }

    public void send(String output) {
        if (!isConnected())
            return;

        try {
            PrintWriter pw = new PrintWriter(outputStream);
            pw.write(output);
            pw.flush();

            /*if (ICTS.config.isDebugEnabled()) {
                CraftBahn.getInstance().debug("Sent:");
                CraftBahn.getInstance().debug(output);
            }*/
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

            if (activeClients.contains(this))
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
