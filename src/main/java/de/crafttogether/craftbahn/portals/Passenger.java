package de.crafttogether.craftbahn.portals;

import java.util.*;

public class Passenger {
    private static final Map<UUID, Passenger> passengers = new HashMap<>();

    private final UUID uuid;
    private final String trainId;
    private final int cartIndex;

    private Passenger(UUID uuid, String trainId, int cartIndex) {
        this.uuid = uuid;
        this.trainId = trainId;
        this.cartIndex = cartIndex;
    }

    public UUID getUUID() { return this.uuid; }
    public String getTrainId() { return this.trainId; }
    public int getCartIndex() { return this.cartIndex; }

    public static Passenger register(UUID uuid, String trainName, int cartIndex) {
        Passenger passenger = new Passenger(uuid, trainName, cartIndex);
        passengers.put(uuid, passenger);
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

    public static Collection<Passenger> get(String trainId) {
        Collection<Passenger> passengerList = new ArrayList<>();

        for (Passenger passenger : passengers.values()) {
            if (passenger.getTrainId().equals(trainId))
                passengerList.add(passenger);
        }

        return passengerList;
    }
}