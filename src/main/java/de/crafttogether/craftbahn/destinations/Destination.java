package de.crafttogether.craftbahn.destinations;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.util.CTLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Destination {
    private String name;
    private Integer id = null;
    private String server = null;
    private String world = null;
    private UUID owner = null;
    private List<UUID> participants = new ArrayList<>();
    private Enum<?> type = null;
    private CTLocation location = null;
    private CTLocation teleportLocation = null;
    private Boolean isPublic = null;

    public enum DestinationType {
        STATION {
            @Override
            public String toString() {
                return Localization.DESTINATIONTYPE_STATION.get();
            }
        },

        MAIN_STATION {
            @Override
            public String toString() {
                return Localization.DESTINATIONTYPE_MAIN_STATION.get();
            }
        },

        PLAYER_STATION {
            @Override
            public String toString() {
                return Localization.DESTINATIONTYPE_PLAYER_STATION.get();
            }
        },

        PUBLIC_STATION {
            @Override
            public String toString() { return Localization.DESTINATIONTYPE_PUBLIC_STATION.get(); }
        }
    }

    public Destination(String name, Integer id) {
        this.id = id;
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
        if (type != DestinationType.MAIN_STATION && type != DestinationType.PLAYER_STATION && type != DestinationType.STATION && type != DestinationType.PUBLIC_STATION)
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
        List<DestinationType> result = Arrays.stream(DestinationType.values())
                .filter(destinationType -> destinationType.toString().equals(label))
                .toList();
        return result.get(0);
    }

    public String toString() {
        StringBuilder strParticipants = new StringBuilder();
        for (UUID participant : participants) strParticipants.append(participant.toString()).append(",");
        if (strParticipants.length() > 1) strParticipants = new StringBuilder(strParticipants.substring(0, strParticipants.length() - 1));
        return "id=" + id + ", name=" + name + ", server=" + server + ", world=" + world + ", type=" + (type == null ? null : type.toString()) + ", owner=" + owner + ", participants=[" + strParticipants + "], isPrivate=" + isPublic + ", location=[" + (location == null ? null : location.toString()) + "], teleportLocation=[" + (teleportLocation == null ? null : teleportLocation.toString()) + "]";
    }
}
