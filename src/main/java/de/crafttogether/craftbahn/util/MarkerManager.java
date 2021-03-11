package de.crafttogether.craftbahn.util;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class MarkerManager {
    public static void deleteMarker(Destination dest) {
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
        MarkerAPI markerApi = dynmap.getMarkerAPI();

        if (dynmap == null)
            return;

        MarkerIcon iconRail = markerApi.getMarkerIcon("ct-rail");
        MarkerIcon iconMinecart = markerApi.getMarkerIcon("ct-minecart");

        if (iconRail == null) {
            iconRail = dynmap.getMarkerAPI().createMarkerIcon("ct-rail", "ct-rail", plugin.getResource("rail.png"));
            dynmap.getMarkerAPI().getMarkerIcons().add(iconRail);
        }

        if (iconMinecart == null) {
            iconMinecart = dynmap.getMarkerAPI().createMarkerIcon("ct-minecart", "ct-minecart", plugin.getResource("minecart.png"));
            dynmap.getMarkerAPI().getMarkerIcons().add(iconMinecart);
        }

        for (Destination.DestinationType type : Destination.DestinationType.values()) {
            MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + type.name());
            String label = "Bahnhof";
            if (type.name().equals("MAIN_STATION")) {
                label = "Hauptbahnhof";
            } else if (type.name().equals("PUBLIC_STATION")) {
                label = "Bahnhof (Öffentlich)";
            } else if (type.name().equals("PLAYER_STATION")) {
                label = "Bahnhof (Spieler)";
            }
            if (set == null)
                set = dynmap.getMarkerAPI().createMarkerSet("CT_" + type.name(), label, null, true);
        }
    }

    public static void setMarker(Destination dest) {
        setMarker(dest, false);
    }

    public static void setMarker(Destination dest, boolean updateOnly) {
        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();

        if (dynmap == null)
            return;

        MarkerAPI markerApi = dynmap.getMarkerAPI();

        if (!updateOnly)
            createMarkerSets();

        plugin.getLogger().info("Create Marker for '" + dest.getName() + "' updateOnly: " + updateOnly);

        MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + dest.getType().name());
        MarkerIcon icon = null;
        String label = null;
        String owner = Bukkit.getOfflinePlayer(dest.getOwner()).getName();
        Boolean showOwner = Boolean.valueOf(true);

        switch (dest.getType().name()) {
            case "STATION":
                icon = markerApi.getMarkerIcon("ct-rail");
                label = "Bahnhof";
                showOwner = Boolean.valueOf(false);
                break;
            case "MAIN_STATION":
                icon = markerApi.getMarkerIcon("ct-rail");
                label = "Hauptbahnhof";
                showOwner = Boolean.valueOf(false);
                break;
            case "PUBLIC_STATION":
                icon = markerApi.getMarkerIcon("ct-rail");
                label = "Öffentlicher Bahnhof";
                showOwner = Boolean.valueOf(false);
                break;
            case "PLAYER_STATION":
                icon = markerApi.getMarkerIcon("ct-minecart");
                label = "Spielerbahnhof";
                showOwner = Boolean.valueOf(true);
                break;
        }

        if (owner == null)
            showOwner = Boolean.valueOf(false);

        label = "<div class=\"ctdestination\" id=\"" + dest.getName() + "\"><div style=\"padding:6px\"><h3 style=\"padding:0px;margin:0px;color:#ffaa00\">" + dest.getName() + " <span style=\"color:#aaaaaa\">(" + label + ")</span></h3>" + (showOwner.booleanValue() ? ("<span style=\"font-weight:bold;color:#aaaaaa;\">Besitzer:</span> " + owner + "<br>") : "") + "<span style=\"font-style:italic;font-weight:bold;color:#ff5555\">/fahrziel " + dest.getName() + "</span></div></div>";
        deleteMarker(dest);
        Location loc = dest.getLocation().getBukkitLocation();
        set.createMarker(dest.getName(), label, true, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
    }
}
