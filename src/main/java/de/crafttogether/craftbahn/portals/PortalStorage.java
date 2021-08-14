package de.crafttogether.craftbahn.portals;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.Callback;
import de.crafttogether.craftbahn.util.MySQLAdapter;

import java.sql.SQLException;
import java.util.TreeMap;

public class PortalStorage {
    private final CraftBahn plugin = CraftBahn.getInstance();
    private final TreeMap<Integer, Portal> portals = new TreeMap<>();

    public void insert(Portal portal, Callback<SQLException, Portal> callback) {
        MySQLAdapter.MySQLConnection MySQL = plugin.getMySQLAdapter().getConnection();

        MySQL.insertAsync("INSERT INTO `%sportals` " +
        "(" +
            "`name`, " +
            "`target_host`, " +
            "`target_port`, " +
            "`target_server`, " +
            "`target_world`, " +
            "`target_x`, " +
            "`target_y`, " +
            "`target_z`" +
        ") " +

        "VALUES (" +
            "'" + portal.getName() + "', " +
            "'" + portal.getTargetHost() + "', "
                + portal.getTargetPort() + ", " +
            "'" + portal.getTargetLocation().getServer() + "', " +
            "'" + portal.getTargetLocation().getWorld() + "', "
                + portal.getTargetLocation().getX() + ", "
                + portal.getTargetLocation().getY() + ", "
                + portal.getTargetLocation().getZ() +
        ");",

        (err, lastInsertedId) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL:] Error: " + err.getMessage());

            // Add to cache
            portal.setId(lastInsertedId);
            portals.put(lastInsertedId, portal);

            callback.call(err, portal);
        }, MySQL.getTablePrefix());
    }

    public void update(Portal portal, Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = plugin.getMySQLAdapter().getConnection();

        MySQL.updateAsync("UPDATE `%sportals` SET " +
            "`name`             = '" + portal.getName() + "', " +
            "`target_host`      = '" + portal.getTargetHost() + "', " +
            "`target_port`      =  " + portal.getTargetPort() + ", " +
            "`target_server`    = '" + portal.getTargetLocation().getServer() + ", " +
            "`target_world`     = '" + portal.getTargetLocation().getWorld() + "', " +
            "`target_x`         =  " + portal.getTargetLocation().getX() + ", " +
            "`target_y`         =  " + portal.getTargetLocation().getY() + ", " +
            "`target_z`         =  " + portal.getTargetLocation().getZ() +
            "WHERE `%sportals`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL:] Error: " + err.getMessage());

            callback.call(err, affectedRows);
            MySQL.close();
        }, MySQL.getTablePrefix(), MySQL.getTablePrefix(), portal.getId());
    }

    public void delete(int portalId, Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = plugin.getMySQLAdapter().getConnection();

        MySQL.updateAsync("DELETE FROM `%sportals` WHERE `id` = %s", (err, affectedRows) -> {
            if (err != null) {
                plugin.getLogger().warning("[MySQL:] Error: " + err.getMessage());
                callback.call(err, null);
            }
            else {
                // Update cache
                portals.remove(portalId);

                callback.call(null, affectedRows);
                MySQL.close();
            }
        }, MySQL.getTablePrefix(), portalId);
    }
}
