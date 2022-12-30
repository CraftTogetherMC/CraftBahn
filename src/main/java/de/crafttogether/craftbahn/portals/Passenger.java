package de.crafttogether.craftbahn.portals;

import de.crafttogether.craftbahn.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Passenger implements Serializable {
    private static final Map<UUID, Passenger> passengers = new HashMap<>();
    private static final Map<UUID, Component> errors = new HashMap<>();

    private final UUID trainId;
    private String trainName = null;
    private final UUID uuid;
    private final EntityType type;
    private final int cartIndex;

    public Passenger(UUID trainId, UUID uuid, EntityType type, int cartIndex) {
        this.trainId = trainId;
        this.uuid = uuid;
        this.type = type;
        this.cartIndex = cartIndex;
    }

    public boolean hasError() {
        return errors.get(trainId) != null;
    }

    public Component getError() {
        return errors.get(trainId);
    }

    public UUID getTrainId() {
        return trainId;
    }
    public String getTrainName() { return trainName; }
    public UUID getUUID() { return this.uuid; }
    public EntityType getType() { return type; }
    public int getCartIndex() { return this.cartIndex; }

    public static Passenger register(Passenger passenger) {
        passengers.put(passenger.getUUID(), passenger);
        return passenger;
    }

    public static void error(UUID trainId, Component error) {
        errors.put(trainId, error);

        // Send to online users
        sendMessage(trainId, error);
    }

    public static void setTrainName(UUID trainId, String trainName) {
        for (Passenger passenger : passengers.values()) {
            if (passenger.trainId.equals(trainId))
                passenger.trainName = trainName;
        }
    }

    public static void sendMessage(UUID trainId, Component message) {
        List<Passenger> passengerList = passengers.values().stream()
                .filter(passenger -> passenger.type.equals(EntityType.PLAYER))
                .filter(passenger -> passenger.trainId.equals(trainId))
                .toList();

        for (Passenger passenger : passengerList) {
            Player player = Bukkit.getPlayer(passenger.getUUID());
            if (player == null || !player.isOnline()) continue;
            player.sendMessage(message);
        }
    }

    public static void remove(UUID uuid) {
        passengers.remove(uuid);
    }

    public static Passenger get(UUID uuid) {
        if (passengers.containsKey(uuid))
            return passengers.get(uuid);
        return null;
    }
}