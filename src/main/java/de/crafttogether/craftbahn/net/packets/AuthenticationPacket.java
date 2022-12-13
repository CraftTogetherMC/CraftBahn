package de.crafttogether.craftbahn.net.packets;

public class AuthenticationPacket implements Packet {
    public String server;
    public String key;

    public AuthenticationPacket(String server, String key) {
        this.server = server;
        this.key = key;
    }
}
