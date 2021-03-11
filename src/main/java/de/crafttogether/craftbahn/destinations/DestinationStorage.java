package de.crafttogether.craftbahn.destinations;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.MySQLAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DestinationStorage {
    private static HashMap<Integer, Destination> destinations = new HashMap<>();

    private static void insert(Destination destination, Destination.Callback<SQLException, Integer> callback) {
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
            "'" + destination.getType() + "', " +
            "'" + destination.getServer() + "', " +
            "'" + destination.getWorld() + "', " +
            (loc != null ? loc.getX() : null) + ", " +
            (loc != null ? loc.getY() : null) + ", " +
            (loc != null ? loc.getZ() : null) + ", " +
            "'" + destination.getOwner().toString() + "', " +
            "'" + participants.toString() + "', " +
            (destination.isPublic() ? 1 : 0) + ", " +
            (tpLoc != null ? loc.getX() : null) + ", " +
            (tpLoc != null ? loc.getY() : null) + ", " +
            (tpLoc != null ? loc.getZ() : null) +
        ");",

        (err, lastInsertedId) -> {
            if (err != null)
                Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());

            // Add to cache
            destination.setId(lastInsertedId);
            destinations.put(lastInsertedId, destination);

            callback.call(err, lastInsertedId);
        });
    }

    public static void update(Destination destination, Destination.Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        CTLocation loc = destination.getLocation();
        CTLocation tpLoc = destination.getTeleportLocation();
        JSONArray participants = new JSONArray();

        for (UUID uuid : destination.getParticipants())
            participants.put(uuid.toString());

        MySQL.updateAsync("UPDATE `%sdestinations` SET " +
            "`name`         = '" + destination.getName() + "', " +
            "`type`         = '" + destination.getType() + "', " +
            "`server`       = '" + destination.getServer() + "', " +
            "`world`        = '" + destination.getWorld() + "', " +
            "`loc_x`        = " + loc.getX() + ", " +
            "`loc_y`        = " + loc.getY() + ", " +
            "`loc_z`        = " + loc.getZ() + ", " +
            "`owner`        = '" + destination.getOwner().toString() + "', " +
            "`participants` = '" + participants.toString() + "', " +
            "`public`       = " + (destination.isPublic() ? 1 : 0) + ", " +
            "`tp_x`         = " + tpLoc.getX() + ", " +
            "`tp_y`         = " + tpLoc.getY() + ", " +
            "`tp_z`         = " + tpLoc.getZ() + " " +
        "WHERE `cb_destinations`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null)
                Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());

            callback.call(err, affectedRows);
            MySQL.close();
        }, MySQL.getTablePrefix(), destination.getId());
    }

    // Unused
    public static void load(int destinationId, Destination.Callback<SQLException, Destination> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        MySQL.queryAsync("SELECT * FROM `%sdestinations` WHERE `id` = %s", (err, result) -> {
            if (err != null) {
                Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());
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
                    Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());
                    return;
                }
                finally {
                    MySQL.close();
                }

                callback.call(err, dest);
            }
        }, MySQL.getTablePrefix(), destinationId);
    }

    public static void delete(int destinationId, Destination.Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        MySQL.updateAsync("DELETE FROM `%sdestinations` WHERE `id` = %s", (err, affectedRows) -> {
            if (err != null) {
                Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());
                callback.call(err, null);
            }
            else {
                // Update cache
                if (destinations.containsKey(destinationId))
                    destinations.remove(destinationId);

                callback.call(err, affectedRows);
                MySQL.close();
            }
        }, MySQL.getTablePrefix(), destinationId);
    }

    public static void loadAll(Destination.Callback<SQLException, Collection<Destination>> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        MySQL.queryAsync("SELECT * FROM `%sdestinations`", (err, result) -> {
            if (err != null) {
                Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());
                callback.call(err, null);
            }

            else {
                try {
                    while (result.next()) {
                        Destination dest = setupDestination(result);
                        destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().warning("[MySQL:] Error: " + ex.getMessage());
                }
                finally {
                    MySQL.close();
                }

                callback.call(err, destinations.values());
            }
        }, MySQL.getTablePrefix());
    }

    public static Collection<Destination> getDestinations() {
        return destinations.values();
    }

    public static Destination getDestination(String destinationName) {
        for (Destination dest : destinations.values())
            if (dest.getName().equalsIgnoreCase(destinationName)) return dest;

        return null;
    }

    public static void addDestination(String name, UUID owner, Destination.DestinationType type, Location loc, Boolean isPublic, Destination.Callback<SQLException, Integer> callback) {
        String serverName = CraftBahn.getInstance().getServerName();
        CTLocation ctLoc = CTLocation.fromBukkitLocation(loc);

        Destination dest = new Destination(name, serverName, loc.getWorld().getName(), owner, new ArrayList<UUID>(), type, ctLoc, ctLoc, isPublic);
        insert(dest, callback);
    }

    private static Destination setupDestination(ResultSet result) {
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
                Bukkit.getLogger().warning("Error: Unable to read participants for '" + name + "'");
            }

            dest = new Destination(name);
            dest.setId(result.getInt("id"));
            dest.setServer(server);
            dest.setWorld(world);
            dest.setOwner(UUID.fromString(result.getString("owner")));
            dest.setParticipants(participants);
            dest.setType(Destination.findType(result.getString("type")));
            dest.setLocation(loc);
            dest.setTeleportLocation(tpLoc);
            dest.setPublic(result.getBoolean("public"));
        }
        catch (Exception err) {
            Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());
        }

        return dest;
    }
}
