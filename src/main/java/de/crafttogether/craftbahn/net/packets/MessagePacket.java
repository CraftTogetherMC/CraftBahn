package de.crafttogether.craftbahn.net.packets;

public class MessagePacket implements Packet {
    public String message;

    public MessagePacket(String message) {
        this.message = message;
    }
}
