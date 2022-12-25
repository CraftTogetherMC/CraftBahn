package de.crafttogether.craftbahn.portals;

import org.bukkit.entity.EntityType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Passenger implements Serializable {
    private static final Map<UUID, Passenger> passengers = new HashMap<>();

    private String trainName;
    private final UUID uuid;
    private final EntityType type;
    private final int cartIndex;

    public Passenger(UUID uuid, EntityType type, int cartIndex) {
        this.uuid = uuid;
        this.type = type;
        this.cartIndex = cartIndex;
    }

    public String getTrainName() {
        return trainName;
    }
    public UUID getUUID() { return this.uuid; }
    public EntityType getType() {
        return type;
    }
    public int getCartIndex() { return this.cartIndex; }

    private void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public static Passenger register(Passenger passenger, String trainName) {
        passenger.setTrainName(trainName);
        passengers.put(passenger.getUUID(), passenger);
        return passenger;
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