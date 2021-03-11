package de.crafttogether.craftbahn.destinations;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.MySQLAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONArray;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DestinationStorage {
    private static List<Destination> destinations = new ArrayList<Destination>();

    public static void insert(Destination destination, Destination.Callback<SQLException, Boolean> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        CTLocation loc = destination.getLocation();
        CTLocation tpLoc = destination.getTeleportLocation();
        JSONArray participants = new JSONArray();

        for (UUID uuid : destination.getParticipants())
            participants.put(uuid.toString());

        Bukkit.getLogger().info("Insert " + destination.getName() + " into `cb_destinations` ...");

        MySQL.executeAsync("INSERT INTO `cb_destinations` " +
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

        (err, result) -> {
            if (err != null)
                Bukkit.getLogger().warning("[MySQL:] Error: " + err.getMessage());
            else
                Bukkit.getLogger().info("Inserted " + destination.getName());

            callback.call(err, result);
        });
    }

    public static void update(Destination destination, Destination.Callback<SQLException, Boolean> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        // TODO: MySQL.updateAsync();
    }

    public static void load(String destinationName, Destination.Callback<SQLException, Destination> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        // TODO: MySQL.queryAsync();
    }

    public static void delete(String destinationName, Destination.Callback<SQLException, Destination> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        // TODO: MySQL.executeAsync();
    }

    public static void loadAll(Destination.Callback<SQLException, List<Destination>> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        MySQL.queryAsync("SELECT * FROM `%sdestinations`", (err, result) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }

            try {
                List<Destination> found = new ArrayList<Destination>();

                while (result.next()) {
                    String name = result.getString("name");
                    String server = result.getString("server");
                    String world = result.getString("world");

                    CTLocation loc = new CTLocation(server, world, result.getDouble("loc_x"), result.getDouble("loc_y"), result.getDouble("loc_z"));
                    CTLocation tpLoc = new CTLocation(server, world, result.getDouble("tp_x"), result.getDouble("tp_y"), result.getDouble("tp_z"));
                    List<UUID> participants = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(result.getString("participants"));
                        for (Object uuid : jsonArray) participants.add(UUID.fromString((String) uuid));
                    }
                    catch (Exception e) {
                        Bukkit.getLogger().warning("Error: Unable to read participants for '" + name + "'");
                    }

                    Destination dest = new Destination(name);
                    dest.setServer(server);
                    dest.setWorld(world);
                    dest.setOwner(UUID.fromString(result.getString("owner")));
                    dest.setParticipants(participants);
                    dest.setType(Destination.findType(result.getString("type")));
                    dest.setLocation(loc);
                    dest.setTeleportLocation(tpLoc);
                    dest.setPublic(result.getBoolean("public"));

                    found.add(dest);
                }

                callback.call(null, found);

            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            MySQL.close();
        }, MySQL.getTablePrefix());
    }

    public static List<Destination> getDestinations() {
        return destinations;
    }

    public static Destination getDestination(String destinationName) {
        for (Destination dest : destinations)
            if (dest.getName().equalsIgnoreCase(destinationName)) return dest;

        return null;
    }

    public static void addDestination(String name, UUID owner, Destination.DestinationType type, Location loc, Boolean isPublic, Destination.Callback<SQLException, Boolean> callback) {
        String serverName = CraftBahn.getInstance().getServerName();
        CTLocation ctLoc = CTLocation.fromBukkitLocation(loc);

        Destination dest = new Destination(name, serverName, loc.getWorld().getName(), owner, new ArrayList<UUID>(), type, ctLoc, ctLoc, isPublic);
        insert(dest, callback);
    }

    public static void deleteDestination(String name) {

    }
}
