package de.crafttogether.craftbahn.destinations;

import de.crafttogether.craftbahn.util.CTLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Destination {
    private Integer id = null;
    private String name = null;
    private String server = null;
    private String world = null;
    private UUID owner = null;
    private List<UUID> participants = new ArrayList<UUID>();
    private Enum<?> type = null;
    private CTLocation location = null;
    private CTLocation teleportLocation = null;
    private Boolean isPublic = null;

    public interface Callback<E extends Throwable, V extends Object> {
        void call(E exception, V result);
    }

    public enum DestinationType {
        STATION {
            @Override
            public String toString() {
                return "Bahnhof";
            }
        },

        MAIN_STATION {
            @Override
            public String toString() {
                return "Hauptbahnhof";
            }
        },

        PUBLIC_STATION {
            @Override
            public String toString() {
                return "Öffentliches Ziel";
            }
        },

        PLAYER_STATION {
            @Override
            public String toString() {
                return "Spielerbahnhof";
            }
        }
    }

    public Destination(String name) {
        this.name = name;
    }

    public Destination(String name, String server, String world, UUID owner, List<UUID> participants, Enum<?> type, CTLocation location, CTLocation teleportLocation, Boolean isPublic) {
        this.name = name;
        this.server = server;
        this.world = world;
        this.owner = owner;
        this.participants = participants;
        this.type = type;
        this.location = location;
        this.teleportLocation = teleportLocation;
        this.isPublic = isPublic;
    }

    public Integer getId() { return id; }

    public String getName() {
        return this.name;
    }

    public String getServer() {
        return this.server;
    }

    public String getWorld() {
        return this.world;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public List<UUID> getParticipants() { return participants; }

    public Enum<?> getType() {
        return this.type;
    }

    public CTLocation getLocation() { return this.location; }

    public CTLocation getTeleportLocation() {
        return teleportLocation;
    }

    public Boolean isPublic() {
        return this.isPublic;
    }

    public void setId(Integer id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setParticipants(List<UUID> participants) { this.participants = participants; }

    public void addParticipant(UUID uuid) { this.participants.add(uuid); }

    public void removeParticipant(UUID uuid) { this.participants.remove(uuid); }

    public void setType(Enum<?> type) {
        if (type != DestinationType.MAIN_STATION && type != DestinationType.PLAYER_STATION && type != DestinationType.STATION)
            return;
        this.type = type;
    }

    public void setType(String type) {
        if (!type.equalsIgnoreCase("MAIN_STATION")
                && !type.equalsIgnoreCase("PLAYER_STATION")
                && !type.equalsIgnoreCase("PUBLIC_STATION")
                && !type.equalsIgnoreCase("STATION"))
            return;

        this.type = DestinationType.valueOf(type);
    }

    public void setLocation(CTLocation location) {
        this.location = location;
    }

    public void setTeleportLocation(CTLocation teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public static DestinationType findType(String label) {
        switch (label.toLowerCase()) {
            case "bahnhof": return DestinationType.STATION;
            case "hauptbahnhof": return DestinationType.MAIN_STATION;
            case "öffentlich": return DestinationType.PUBLIC_STATION;
            case "spielerbahnhof": return DestinationType.PLAYER_STATION;
        }
        return null;
    }

    public String toString() {
        String strParticipants = "";
        for (UUID participant : participants) strParticipants += participant.toString() + ",";
        if (strParticipants.length() > 1) strParticipants = strParticipants.substring(-1);
        return "id=" + id + ", name=" + name + ", server=" + server + ", world=" + world + ", type=" + (type == null ? null : type.toString()) + ", owner=" + owner + ", participants=[" + strParticipants + "], isPrivate=" + isPublic + ", location=[" + (location == null ? null : location.toString()) + "], teleportLocation=[" + (teleportLocation == null ? null : teleportLocation.toString()) + "]";
    }
}
