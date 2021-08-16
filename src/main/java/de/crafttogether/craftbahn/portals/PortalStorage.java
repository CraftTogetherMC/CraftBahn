package de.crafttogether.craftbahn.portals;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.Callback;
import de.crafttogether.craftbahn.util.MySQLAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

public class PortalStorage {
    private final CraftBahn plugin = CraftBahn.getInstance();
    private final TreeMap<Integer, Portal> portals = new TreeMap<>();

    public PortalStorage() {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();
    }

    public void getOrCreate(String portalName, Callback<SQLException, Portal> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        try {
            ResultSet result = MySQL.query("SELECT * FROM `%sportals` WHERE `name` = '%s'", MySQL.getTablePrefix(), portalName);

            if (result.next()) {
                Portal portal = new Portal(result.getString("name"), result.getInt("id"));

                portal.setTargetPort(result.getInt("target_port"));
                portal.setTargetHost(result.getString("target_host"));
                portal.setTargetLocation(new CTLocation(
                        result.getString("target_server"),
                        result.getString("target_world"),
                        result.getDouble("target_x"),
                        result.getDouble("target_y"),
                        result.getDouble("target_z")
                ));

                // Update cache
                portals.put(portal.getId(), portal);

                callback.call(null, portal);
            } else {
                int portalId = MySQL.insert("INSERT INTO `%sportals` SET `name` = '%s'", MySQL.getTablePrefix(), portalName);
                Portal portal = new Portal(portalName, portalId);

                // Update cache
                portals.put(portalId, portal);

                callback.call(null, portal);
            }
        }
        catch (SQLException err) {
            callback.call(err, null);
        }
        finally {
            MySQL.close();
        }
    }

    public void get(String portalName, Callback<SQLException, Portal> callback) {
        MySQLAdapter.MySQLConnection MySQL = CraftBahn.getInstance().getMySQLAdapter().getConnection();

        try {
            ResultSet result = MySQL.query("SELECT * FROM `%sportals` WHERE `name` = '%s'", MySQL.getTablePrefix(), portalName);

            if (result.next()) {
                Portal portal = new Portal(result.getString("name"), result.getInt("id"));

                portal.setTargetPort(result.getInt("target_port"));
                portal.setTargetHost(result.getString("target_host"));
                portal.setTargetLocation(new CTLocation(
                    result.getString("target_server"),
                    result.getString("target_world"),
                    result.getDouble("target_x"),
                    result.getDouble("target_y"),
                    result.getDouble("target_z")
                ));

                // Update cache
                portals.put(portal.getId(), portal);

                callback.call(null, portal);
            }
        }
        catch (SQLException err) {
            callback.call(err, null);
        }
        finally {
            MySQL.close();
        }
    }

    public void update(Portal portal, Callback<SQLException, Integer> callback) {
        MySQLAdapter.MySQLConnection MySQL = plugin.getMySQLAdapter().getConnection();

        MySQL.updateAsync("UPDATE `%sportals` SET " +
            "`name`             = '" + portal.getName() + "', " +
            "`target_host`      = '" + portal.getTargetHost() + "', " +
            "`target_port`      =  " + portal.getTargetPort() + ", " +
            "`target_server`    = '" + portal.getTargetLocation().getServer() + "', " +
            "`target_world`     = '" + portal.getTargetLocation().getWorld() + "', " +
            "`target_x`         =  " + portal.getTargetLocation().getX() + ", " +
            "`target_y`         =  " + portal.getTargetLocation().getY() + ", " +
            "`target_z`         =  " + portal.getTargetLocation().getZ() +
            "WHERE `%sportals`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null)
                plugin.getLogger().warning("[MySQL:] Error: " + err.getMessage());

            // Update cache
            portals.put(portal.getId(), portal);

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
            }

            MySQL.close();
        }, MySQL.getTablePrefix(), portalId);
    }
}
