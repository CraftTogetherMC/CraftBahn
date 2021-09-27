package de.crafttogether.craftbahn.portals;

import de.crafttogether.Callback;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.mysql.MySQLConnection;
import de.crafttogether.mysql.MySQLPool;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.TreeMap;

public class PortalStorage {
    private final CraftBahnPlugin plugin = CraftBahnPlugin.getInstance();
    private final MySQLPool mySQLPool = plugin.getMySQLPool();
    private final TreeMap<Integer, Portal> portals = new TreeMap<>();

    public PortalStorage() {
        MySQLConnection MySQL = mySQLPool.getConnection();

        // Create Tables if missing
        try {
            ResultSet result = MySQL.query("SHOW TABLES LIKE '%sportals';", MySQL.getTablePrefix());

            if (!result.next()) {
                plugin.getLogger().info("[MySQL]: Create Table '" + MySQL.getTablePrefix() + "portals' ...");

                MySQL.execute("""
                    CREATE TABLE `%sportals` (
                      `id` int(11) NOT NULL,
                      `name` int(16) NOT NULL,
                      `target_host` int(255) DEFAULT NULL,
                      `target_port` int(11) DEFAULT NULL,
                      `target_server` int(24) DEFAULT NULL,
                      `target_world` varchar(24) DEFAULT NULL,
                      `target_x` double DEFAULT NULL,
                      `target_y` double DEFAULT NULL,
                      `target_z` double DEFAULT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
                """, MySQL.getTablePrefix());

                MySQL.execute("""
                     ALTER TABLE `%sportals`
                       ADD PRIMARY KEY (`id`),
                       ADD UNIQUE KEY `name` (`name`);
                """, MySQL.getTablePrefix());

                MySQL.execute("""
                    ALTER TABLE `%sportals`
                      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
                """, MySQL.getTablePrefix());
            }
        }
        catch (SQLException ex) {
            plugin.getLogger().warning("[MySQL]: " + ex.getMessage());
        }
        finally {
            MySQL.close();
        }

        // Load all portals from database into our cache
        Bukkit.getServer().getScheduler().runTask(plugin, () -> loadAll((err, portals) -> {
            plugin.getLogger().info("Loaded " + portals.size() + " Portals");
        }));
    }

    public void getOrCreate(String portalName, Callback<SQLException, Portal> callback) {
        MySQLConnection MySQL = mySQLPool.getConnection();

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
        MySQLConnection MySQL = mySQLPool.getConnection();

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
        MySQLConnection MySQL = mySQLPool.getConnection();

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
        MySQLConnection MySQL = mySQLPool.getConnection();

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

    public void loadAll(Callback<SQLException, Collection<Portal>> callback) {
        MySQLConnection MySQL = mySQLPool.getConnection();

        MySQL.queryAsync("SELECT * FROM `%sportals`", (err, result) -> {
            if (err != null) {
                CraftBahnPlugin.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
                callback.call(err, null);
            }

            else {
                try {
                    while (result.next()) {
                        Portal portal = setupPortal(result);

                        // Update cache
                        portals.put(portal.getId(), portal);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    CraftBahnPlugin.getInstance().getLogger().warning("[MySQL:] Error: " + ex.getMessage());
                }
                finally {
                    MySQL.close();
                }

                callback.call(err, portals.values());
            }
        }, MySQL.getTablePrefix());
    }

    private Portal setupPortal(ResultSet result) {
        Portal portal = null;

        try {
            Integer id = result.getInt("id");
            String name = result.getString("name");
            String server = result.getString("target_server");
            String world = result.getString("target_world");

            double x = result.getDouble("target_x");
            double y = result.getDouble("target_y");
            double z = result.getDouble("target_z");
            CTLocation targetLocation = new CTLocation(server, world, x, y, z);

            portal = new Portal(name, id);
            portal.setId(result.getInt("id"));
            portal.setTargetHost(result.getString("target_host"));
            portal.setTargetPort(result.getInt("target_port"));
            portal.setTargetLocation(targetLocation);
        }
        catch (Exception err) {
            CraftBahnPlugin.getInstance().getLogger().warning("[MySQL:] Error: " + err.getMessage());
        }

        return portal;
    }

    public Collection<Portal> getPortals() {
        return portals.values();
    }

    public Portal getPortal(String name) {
        for (Portal portal : portals.values()) {
            if (portal.getName().equalsIgnoreCase(name))
                return portal;
        }

        return null;
    }

    public Portal getPortal(int id) {
        for (Portal portal : portals.values())
            if (portal.getId() == id) return portal;

        return null;
    }
}
