package de.crafttogether.craftbahn.util;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

public class DynmapMarker {
    private static final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;

    public static int setupMarkers(Collection<Destination> destinations) {
        if (plugin.getDynmap() == null)
            return 0;

        plugin.getLogger().info("Setup Markers...");

        int markersCreated = 0;
        for (Destination destination : destinations) {
            if (!plugin.getServerName().equalsIgnoreCase(destination.getServer())) continue;
            if(addMarker(destination)) markersCreated++;
        }

        plugin.getLogger().info("Created " + markersCreated + " markers.");
        plugin.getLogger().info("Marker-Setup completed.");
        return markersCreated;
    }

    public static void deleteMarker(Destination dest) {
        if (plugin.getDynmap() == null)
            return;

        if (!dest.getServer().equalsIgnoreCase(plugin.getServerName()))
            return;

        MarkerSet set = plugin.getDynmap().getMarkerAPI().getMarkerSet("CB_" + dest.getType().name());
        if (set == null)
            return;

        Marker marker = set.findMarker(dest.getName());
        if (marker != null)
            marker.deleteMarker();
    }

    public static boolean addMarker(Destination destination) {
        if (plugin.getDynmap() == null)
            return false;

        if (!destination.getServer().equalsIgnoreCase(plugin.getServerName()))
            return false;

        DynmapAPI dynmap = plugin.getDynmap();

        if (dynmap == null)
            return false;

        MarkerAPI markers = dynmap.getMarkerAPI();
        MarkerSet set = markers.getMarkerSet("CT_" + destination.getType().name());

        // Create MarkerSet if not exists
        if (set == null)
            set = dynmap.getMarkerAPI().createMarkerSet("CT_" + destination.getType().name(), destination.getType().toString(), null, true);

        // Delete Marker if already exists
        Marker marker = set.findMarker(destination.getName());
        if (marker != null)
            marker.deleteMarker();

        if (Bukkit.getServer().getWorld(destination.getLocation().getWorld()) == null) {
            plugin.getLogger().warning("Error: Unable to create marker for '" + destination.getName() + "'. World '" + destination.getWorld() + "' is not loaded");
            return false;
        }

        if (destination.getLocation() == null) {
            plugin.getLogger().warning("Error: Destination '" + destination.getName() + "' has no location set!");
            return false;
        }

        MarkerIcon icon, railIcon, minecartIcon;
        String label, owner, color = null;
        boolean showOwner = true;

        // Load icons
        railIcon = markers.getMarkerIcon("cbRail");
        if (railIcon == null)
            railIcon = markers.createMarkerIcon("cbRail", "Rail", plugin.getResource(plugin.getDataFolder() + File.separator + "rail.png"));

        minecartIcon = markers.getMarkerIcon("cbMinecart");
        if (minecartIcon == null)
            minecartIcon = markers.createMarkerIcon("cbMinecart", "Minecart", plugin.getResource(plugin.getDataFolder() + File.separator + "minecart.png"));

        StringBuilder participants = new StringBuilder(Bukkit.getOfflinePlayer(destination.getOwner()).getName() + ", ");
        for (UUID uuid : destination.getParticipants()) {
            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
            if (!participant.hasPlayedBefore()) continue;
            participants.append(participant.getName()).append(", ");
        }
        owner = participants.isEmpty() ? "" : participants.toString().substring(0, participants.length() - 2);

        icon = minecartIcon;
        switch (destination.getType().name()) {
            case "STATION", "MAIN_STATION", "PUBLIC_STATION" -> {
                color = "#ffaa00";
                icon = railIcon;
                showOwner = false;
            }
            case "PLAYER_STATION" ->  color = "#ffff55";
        }

        label = "<div style=\"z-index:99999\">" +
                    "<div style=\"padding:6px\">" +
                        "<h3 style=\"padding:0px;margin:0px;color:" + color + "\">" + destination.getName() + "</h3>" +
                        "<span style=\"font-weight:bold;color:#aaaaaa;\">Stations-Typ:</span> " + destination.getType() + "<br>" +
                        (showOwner ? ("<span style=\"font-weight:bold;color:#aaaaaa;\">Besitzer:</span> " + owner + "<br>") : "") +
                        "<span style=\"font-style:italic;font-weight:bold;color:#ffaa00\">/fahrziel <span style=\"color:#ffff55\">" + destination.getName() + "</span></span>" +
                        "</div>" +
                "</div>";


        Location location = destination.getLocation().getBukkitLocation();
        set.createMarker(destination.getName(), label, true, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), icon, false);
        return true;
    }
}