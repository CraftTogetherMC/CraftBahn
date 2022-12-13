package de.crafttogether.craftbahn.net.events;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityReceivedEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final UUID uuid;
    private final EntityType type;
    private final CommonTagCompound tagCompound;

    public EntityReceivedEvent(UUID uuid, EntityType type, CommonTagCompound tagCompound) {
        this.uuid = uuid;
        this.type = type;
        this.tagCompound = tagCompound;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EntityType getType() {
        return type;
    }

    public CommonTagCompound getTagCompound() {
        return tagCompound;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
