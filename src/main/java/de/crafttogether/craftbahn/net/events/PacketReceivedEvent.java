package de.crafttogether.craftbahn.net.events;

import de.crafttogether.craftbahn.net.packets.Packet;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class PacketReceivedEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Socket connection;
    private final Packet packet;

    public PacketReceivedEvent(Socket connection, Packet packet) {
        this.connection = connection;
        this.packet = packet;
    }

    public Socket getConnection() {
        return connection;
    }

    public Packet getPacket() {
        return packet;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
