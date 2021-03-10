package de.crafttogether.craftbahn.destinations;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.MySQLAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DestinationStorage {
    private static List<Destination> destinations = new ArrayList<Destination>();

    public static void insert(Destination destination, Destination.Callback<SQLException, Boolean> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        // TODO: MySQL.executeAsync();
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

    public static void loadAll(String destinationName, Destination.Callback<SQLException, List<Destination>> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQL();

        MySQL.queryAsync("SELECT * FROM `%sdestinations`", (err, result) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }

            try {
                List<Destination> found = new ArrayList<Destination>();

                if (result.next()) {
                    Destination dest = new Destination(result.getString("name"));
                    dest.setServer(result.getString("server"));
                    dest.setWorld(result.getString("world"));
                    found.add(dest);

                    callback.call(null, new ArrayList<Destination>());
                }
                else
                    callback.call(null, new ArrayList<Destination>());

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

    public static void addDestination(String name, UUID owner, Destination.DestinationType type, Location location, Boolean isPublic) {
        String serverName = CraftBahn.getServerName();
        Destination dest = new Destination(name, serverName, location.getWorld().getName(), owner, new ArrayList<UUID>(), type, location, isPublic);
        insert(dest, (err, success) -> {

        });
    }

    public static void deleteDestination(String name) {

    }
}
