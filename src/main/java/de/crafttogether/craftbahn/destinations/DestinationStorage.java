package de.crafttogether.craftbahn.destinations;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DestinationStorage {
    private final CraftBahn plugin = CraftBahn.getInstance();
    private final TreeMap<Integer, Destination> destinations = new TreeMap<>();

    public DestinationStorage() {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        // Create Tables if missing
        try {
            ResultSet result = MySQL.query("SHOW TABLES LIKE '%sdestinations';", MySQL.getTablePrefix());

            if (!result.next()) {
                plugin.getLogger().info("[MySQL]: Create Table '" + MySQL.getTablePrefix() + "destinations' ...");

                MySQL.execute(
            """
                    CREATE TABLE `%sdestinations` (
                      `id` int(11) NOT NULL,
                      `name` varchar(24) NOT NULL,
                      `type` varchar(24) NOT NULL,
                      `server` varchar(24) NOT NULL,
                      `world` varchar(24) NOT NULL,
                      `loc_x` double NOT NULL,
                      `loc_y` double NOT NULL,
                      `loc_z` double NOT NULL,
                      `owner` varchar(36) NOT NULL,
                      `participants` longtext DEFAULT NULL,
                      `public` tinyint(1) NOT NULL,
                      `tp_x` double DEFAULT NULL,
                      `tp_y` double DEFAULT NULL,
                      `tp_z` double DEFAULT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                    """
                , MySQL.getTablePrefix());

                MySQL.execute(
                "ALTER TABLE `%sdestinations`\n" +
                        "  ADD PRIMARY KEY (`id`);"
                , MySQL.getTablePrefix());

                MySQL.execute(
                "ALTER TABLE `%sdestinations`\n" +
                    "  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;"
                , MySQL.getTablePrefix());
            }
        }
        catch (SQLException ex) {
            plugin.getLogger().warning("[MySQL]: " + ex.getMessage());
        }
        finally {
            MySQL.close();
        }

        // Load all destinations from database into our cache
        Bukkit.getServer().getScheduler().runTask(plugin, () -> loadAll((err, destinations) -> {
            plugin.getLogger().info("Loaded " + destinations.size() + " destinations");

            plugin.getLogger().info("Setup MarkerSets...");
            MarkerManager.createMarkerSets();
            plugin.getLogger().info("Setup Markers...");

            int markersCreated = 0;
            for (Destination dest : destinations) {
                if (!CraftBahn.getInstance().getServerName().equalsIgnoreCase(dest.getServer()))
                    continue;

                if(MarkerManager.addMarker(dest, true))
                    markersCreated++;
            }

            plugin.getLogger().info("Created " + markersCreated + " markers.");
            plugin.getLogger().info("Marker-Setup completed.");
        }));
    }

    private void insert(Destination destination, Callback<SQLException, Destination> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        CTLocation loc = destination.getLocation();
        CTLocation tpLoc = destination.getTeleportLocation();
        JSONArray participants = new JSONArray();

        for (UUID uuid : destination.getParticipants())
            participants.put(uuid.toString());

        MySQL.insertAsync("INSERT INTO `cb_destinations` " +
        "(" +
            "`name`, " +
            "`type`, " +
            "`server`, " +
            "`world`, " +
            "`loc_x`, " +
            "`loc_y`, " +
            "`loc_z`, " +
            "`owner`, " +
            "`participants`, " +
            "`public`, " +
            "`tp_x`, " +
            "`tp_y`, " +
            "`tp_z`" +
        ") " +

        "VALUES (" +
            "'" + destination.getName() + "', " +
            "'" + destination.getType().name() + "', " +
            "'" + destination.getServer() + "', " +
            "'" + destination.getWorld() + "', " +
            (loc != null ? loc.getX() : null) + ", " +
            (loc != null ? loc.getY() : null) + ", " +
            (loc != null ? loc.getZ() : null) + ", " +
            "'" + destination.getOwner().toString() + "', " +
            "'" + participants + "', " +
            (destination.isPublic() ? 1 : 0) + ", " +
            (tpLoc != null ? loc.getX() : null) + ", " +
            (tpLoc != null ? loc.getY() : null) + ", " +
            (tpLoc != null ? loc.getZ() : null) +
        ");",

        (err, lastInsertedId) -> {
            if (err != null)
                CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());

            // Add to cache
            destination.setId(lastInsertedId);
            destinations.put(lastInsertedId, destination);

            callback.call(err, destination);
            MySQL.close();
        });
    }

    public void update(Destination destination, Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        CTLocation loc = destination.getLocation();
        CTLocation tpLoc = destination.getTeleportLocation();
        JSONArray participants = new JSONArray();

        for (UUID uuid : destination.getParticipants())
            participants.put(uuid.toString());

        MySQL.updateAsync("UPDATE `%sdestinations` SET " +
            "`name`         = '" + destination.getName() + "', " +
            "`type`         = '" + destination.getType().name() + "', " +
            "`server`       = '" + destination.getServer() + "', " +
            "`world`        = '" + destination.getWorld() + "', " +
            "`loc_x`        = " + loc.getX() + ", " +
            "`loc_y`        = " + loc.getY() + ", " +
            "`loc_z`        = " + loc.getZ() + ", " +
            "`owner`        = '" + destination.getOwner().toString() + "', " +
            "`participants` = '" + participants + "', " +
            "`public`       = " + (destination.isPublic() ? 1 : 0) + ", " +
            "`tp_x`         = " + tpLoc.getX() + ", " +
            "`tp_y`         = " + tpLoc.getY() + ", " +
            "`tp_z`         = " + tpLoc.getZ() + " " +
        "WHERE `cb_destinations`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null)
                CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());

            callback.call(err, affectedRows);
            MySQL.close();
        }, MySQL.getTablePrefix(), destination.getId());
    }

    // TODO: Trigger if other server updates a destination
    public void load(int destinationId, Callback<SQLException, Destination> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        MySQL.queryAsync("SELECT * FROM `%sdestinations` WHERE `id` = %s", (err, result) -> {
            if (err != null) {
                CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
            }

            else {
                Destination dest = null;

                try {
                    if (result.next()) {
                        dest = setupDestination(result);

                        // Update cache
                        destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
                }
                finally {
                    MySQL.close();
                }

                callback.call(err, dest);
            }
        }, MySQL.getTablePrefix(), destinationId);
    }

    public void delete(int destinationId, Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        MySQL.updateAsync("DELETE FROM `%sdestinations` WHERE `id` = %s", (err, affectedRows) -> {
            if (err != null) {
                CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
                callback.call(err, null);
            }
            else {
                // Update cache
                destinations.remove(destinationId);

                callback.call(null, affectedRows);
                MySQL.close();
            }
        }, MySQL.getTablePrefix(), destinationId);
    }

    public void loadAll(Callback<SQLException, Collection<Destination>> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        MySQL.queryAsync("SELECT * FROM `%sdestinations`", (err, result) -> {
            if (err != null) {
                CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
                callback.call(err, null);
            }

            else {
                try {
                    while (result.next()) {
                        Destination dest = setupDestination(result);

                        // Update cache
                        destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + ex.getMessage());
                }
                finally {
                    MySQL.close();
                }

                callback.call(err, destinations.values());
            }
        }, MySQL.getTablePrefix());
    }

    public Collection<Destination> getDestinations() {
        return destinations.values();
    }

    public Collection<Destination> getDestinations(String name) {
        List<Destination> list = new ArrayList<>();

        for (Destination dest : destinations.values()) {
            if (dest.getName().equalsIgnoreCase(name))
                list.add(dest);
        }

        return list;
    }

    public Destination getDestination(int id) {
        for (Destination dest : destinations.values())
            if (dest.getId() == id) return dest;

        return null;
    }

    public Destination getDestination(String destinationName, String serverName) {
        for (Destination dest : destinations.values())
            if (dest.getName().equalsIgnoreCase(destinationName) && dest.getServer().equalsIgnoreCase(serverName)) return dest;

        return null;
    }

    public List<Destination> filterByServer(List<Destination> destinations, String serverName) {
        List<Destination> list = new ArrayList<>();

        for (Destination dest : destinations) {
            if (dest.getServer().equalsIgnoreCase(serverName))
                list.add(dest);
        }

        return list;
    }

    public List<Destination> filterByType(List<Destination> destinations, Destination.DestinationType type) {
        List<Destination> list = new ArrayList<>();

        for (Destination dest : destinations) {
            if (dest.getType().equals(type))
                list.add(dest);
        }

        return list;
    }

    public void addDestination(String name, UUID owner, Destination.DestinationType type, Location loc, Boolean isPublic, Callback<SQLException, Destination> callback) {
        String serverName = CraftBahn.getInstance().getServerName();
        CTLocation ctLoc = CTLocation.fromBukkitLocation(loc);

        Destination dest = new Destination(name, serverName, loc.getWorld().getName(), owner, new ArrayList<>(), type, ctLoc, ctLoc, isPublic);
        insert(dest, callback);
    }

    private Destination setupDestination(ResultSet result) {
        Destination dest = null;

        try {
            String name = result.getString("name");
            String server = result.getString("server");
            String world = result.getString("world");

            CTLocation loc = new CTLocation(server, world, result.getDouble("loc_x"), result.getDouble("loc_y"), result.getDouble("loc_z"));
            CTLocation tpLoc = new CTLocation(server, world, result.getDouble("tp_x"), result.getDouble("tp_y"), result.getDouble("tp_z"));
            List<UUID> participants = new ArrayList<>();

            try {
                JSONArray jsonArray = new JSONArray(result.getString("participants"));
                for (Object uuid : jsonArray) participants.add(UUID.fromString((String) uuid));
            } catch (Exception e) {
                CraftBahn.getInstance().getLogger().warning("Error: Unable to read participants for '" + name + "'");
            }

            Destination.DestinationType destinationType = Destination.DestinationType.valueOf(result.getString("type"));
            dest = new Destination(name);
            dest.setId(result.getInt("id"));
            dest.setServer(server);
            dest.setWorld(world);
            dest.setOwner(UUID.fromString(result.getString("owner")));
            dest.setParticipants(participants);
            dest.setType(destinationType);
            dest.setLocation(loc);
            dest.setTeleportLocation(tpLoc);
            dest.setPublic(result.getBoolean("public"));
        }
        catch (Exception err) {
            CraftBahn.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
        }

        return dest;
    }
}
