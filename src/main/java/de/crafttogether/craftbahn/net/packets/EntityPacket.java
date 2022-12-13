package de.crafttogether.craftbahn.net.packets;

import org.bukkit.entity.EntityType;

import java.util.UUID;

public class EntityPacket implements Packet {
    public UUID uuid;
    public EntityType type;

    public EntityPacket(UUID uuid, EntityType entityType) {
        this.uuid = uuid;
        this.type = entityType;
    }
}
