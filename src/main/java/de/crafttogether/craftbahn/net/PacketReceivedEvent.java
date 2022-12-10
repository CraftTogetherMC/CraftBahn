package de.crafttogether.craftbahn.net;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class PacketReceivedEvent extends Event {
    private Socket connection;
    private String message;

    public PacketReceivedEvent(Socket connection, String message) {
        this.connection = connection;
        this.message = message;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}
