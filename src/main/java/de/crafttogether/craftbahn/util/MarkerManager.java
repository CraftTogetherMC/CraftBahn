package de.crafttogether.craftbahn.util;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.UUID;

public class MarkerManager {
    public static void deleteMarker(Destination dest) {
        if (!dest.getServer().equalsIgnoreCase(CraftBahn.getInstance().getServerName()))
            return;

        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();

        MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + dest.getType().name());

        if (set == null)
            return;

        Marker marker = set.findMarker(dest.getName());
        if (marker != null)
            marker.deleteMarker();
    }

    public static void createMarkerSets() {
        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();

        if (dynmap == null)
            return;

        for (Destination.DestinationType type : Destination.DestinationType.values()) {
            MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + type.name());
            String label = switch (type.name()) {
                case "MAIN_STATION" -> "Hauptbahnhof";
                case "PUBLIC_STATION" -> "Bahnhof (Öffentlich)";
                case "PLAYER_STATION" -> "Bahnhof (Spieler)";
                default -> "Bahnhof";
            };

            if (set == null)
                dynmap.getMarkerAPI().createMarkerSet("CT_" + type.name(), label, null, true);
        }
    }

    public static boolean addMarker(Destination dest) {
        return addMarker(dest, false);
    }

    public static boolean addMarker(Destination dest, boolean updateOnly) {
        if (!dest.getServer().equalsIgnoreCase(CraftBahn.getInstance().getServerName()))
            return false;

        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();

        if (dynmap == null)
            return false;

        MarkerAPI markerApi = dynmap.getMarkerAPI();

        if (!updateOnly)
            createMarkerSets();

        MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + dest.getType().name());

        if (Bukkit.getServer().getWorld(dest.getLocation().getWorld()) == null) {
            plugin.getLogger().warning("Error: Unable to create marker for '" + dest.getName() + "'. World '" + dest.getWorld() + "' is not loaded");
            return false;
        }

        if (dest.getLocation() == null) {
            plugin.getLogger().warning("Error: Destination '" + dest.getName() + "' has no location set!");
            return false;
        }

        MarkerIcon icon = null;
        String label;
        String color = null;
        Boolean showOwner = Boolean.TRUE;
        StringBuilder strParticipants = new StringBuilder(Bukkit.getOfflinePlayer(dest.getOwner()).getName() + ", ");

        for (UUID uuid : dest.getParticipants()) {
            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
            if (!participant.hasPlayedBefore()) continue;
            strParticipants.append(participant.getName()).append(", ");
        }

        if (strParticipants.length() > 1)
            strParticipants = new StringBuilder(strParticipants.substring(0, strParticipants.length() - 2));

        switch (dest.getType().name()) {
            case "STATION", "MAIN_STATION", "PUBLIC_STATION" -> {
                color = "#ffaa00";
                icon = markerApi.getMarkerIcon("ct-rail");
                showOwner = Boolean.FALSE;
            }
            case "PLAYER_STATION" -> {
                color = "#ffff55";
                icon = markerApi.getMarkerIcon("ct-minecart");
                showOwner = Boolean.TRUE;
            }
        }

        label = "<div style=\"z-index:99999\">" +
                    "<div style=\"padding:6px\">" +
                        "<h3 style=\"padding:0px;margin:0px;color:" + color + "\">" + dest.getName() + "</h3>" +
                        "<span style=\"font-weight:bold;color:#aaaaaa;\">Stations-Typ:</span> " + dest.getType() + "<br>" +
                        (showOwner ? ("<span style=\"font-weight:bold;color:#aaaaaa;\">Besitzer:</span> " + strParticipants + "<br>") : "") +
                        "<span style=\"font-style:italic;font-weight:bold;color:#ffaa00\">/fahrziel <span style=\"color:#ffff55\">" + dest.getName() + "</span></span>" +
                    "</div>" +
                "</div>";

        deleteMarker(dest);

        Location loc = dest.getLocation().getBukkitLocation();
        set.createMarker(dest.getName(), label, true, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);

        return true;
    }
}
