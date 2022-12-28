package de.crafttogether.craftbahn.net.packets;

import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.util.CTLocation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrainPacket implements Packet {
    public UUID id;
    public String oldName;
    public String portalName;
    public Set<String> owners;
    public String properties;
    public CTLocation target;
    public List<Passenger> passengers;
    public String sourceServer;

    public TrainPacket() {}
}