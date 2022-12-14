package de.crafttogether.craftbahn.net.packets;

import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.util.CTLocation;

import java.util.List;
import java.util.Set;

public class TrainPacket implements Packet {
    public String id;
    public String name;
    public String portalName;
    public Set<String> owners;
    public String properties;
    public CTLocation target;
    public List<Passenger> passengers;
    public String sourceServer;

    public TrainPacket() {}
}