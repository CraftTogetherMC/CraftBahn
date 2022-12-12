package de.crafttogether.craftbahn.portals;

import de.crafttogether.Callback;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class PortalStorage {
    private final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;
    private final TreeMap<Integer, Portal> portals = new TreeMap<>();

    public PortalStorage() {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        // Create Tables if missing
        try {
            ResultSet result = MySQL.query("SHOW TABLES LIKE '%sportals';", MySQL.getTablePrefix());

            if (!result.next()) {
                plugin.getLogger().info("[MySQL]: Create Table '" + MySQL.getTablePrefix() + "portals' ...");

                MySQL.execute("""
                    CREATE TABLE `cb_portals` (
                        `id` int(11) NOT NULL,
                        `name` varchar(16) NOT NULL,
                        `type` varchar(16) NOT NULL,
                        `target_host` varchar(128) DEFAULT NULL,
                        `target_port` int(11) DEFAULT NULL,
                        `target_server` varchar(128) DEFAULT NULL,
                        `target_world` varchar(128) DEFAULT NULL,
                        `target_x` double DEFAULT NULL,
                        `target_y` double DEFAULT NULL,
                        `target_z` double DEFAULT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
                """, MySQL.getTablePrefix());

                MySQL.execute("""
                    ALTER TABLE `cb_portals`
                        ADD PRIMARY KEY (`id`),
                        ADD KEY `name` (`name`) USING BTREE;
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

            // Remove not existing signs from database
            Bukkit.getServer().getScheduler().runTask(plugin, this::checkSigns);
        }));
    }

    public void checkSigns() {
        List<Portal> localPortals = portals.values().stream()
                .filter(portal -> portal.getTargetLocation().getServer().equals(plugin.getServerName()))
                .toList();

        for (Portal portal : localPortals) {
            if (portal.getSign() != null) continue;

            delete(portal.getId(), (err, rows) -> plugin.getLogger().info("Deleted portal '" + portal.getName() + "' because action-sign doesn't exist anymore."));
        }
    }

    public List<Portal> get(String portalName) throws SQLException {
        List<Portal> found = new ArrayList<>();

        MySQLConnection MySQL = MySQLAdapter.getConnection();
        ResultSet result = MySQL.query("SELECT * FROM `%sportals` WHERE `name` = '%s'", MySQL.getTablePrefix(), portalName);

        while (result.next()) {
            Portal portal = new Portal(
                    result.getString("name"),
                    Portal.PortalType.valueOf(result.getString("type")),
                    result.getInt("id"),
                    result.getString("target_host"),
                    result.getInt("target_port"),
                    new CTLocation(
                            result.getString("target_server"),
                            result.getString("target_world"),
                            result.getDouble("target_x"),
                            result.getDouble("target_y"),
                            result.getDouble("target_z")
                    ));
            found.add(portal);

            // Update cache
            portals.put(portal.getId(), portal);
        }

        MySQL.close();
        return found;
    }

    public Portal create(String name, Portal.PortalType type, String host, int port, CTLocation location) throws SQLException {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        int portalId = MySQL.insert("INSERT INTO `%sportals` SET " +
                "`name` = '" + name + "', " +
                "`type` = '" + type + "', " +
                "`target_host` = '" + host + "', " +
                "`target_port` = " + port + ", " +
                "`target_server` = '" + location.getServer() + "', " +
                "`target_world` = '" + location.getWorld() + "', " +
                "`target_x` = " + location.getX() + ", " +
                "`target_y` = " + location.getY() + ", " +
                "`target_z` = " + location.getZ(), MySQL.getTablePrefix());
        MySQL.close();

        Portal portal = new Portal(name, type, portalId, host, port, location);

        // Update cache
        portals.put(portalId, portal);
        return portal;
    }

    public void update(Portal portal, Callback<SQLException, Integer> callback) {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        MySQL.updateAsync("UPDATE `%sportals` SET " +
                "`name`             = '" + portal.getName() + "', " +
                "`type`             = '" + portal.getType().name() + "', " +
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
        MySQLConnection MySQL = MySQLAdapter.getConnection();

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
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        MySQL.queryAsync("SELECT * FROM `%sportals`", (err, result) -> {
            if (err != null) {
                plugin.getLogger().warning("[MySQL:] Error: " + err.getMessage());
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
                    plugin.getLogger().warning("[MySQL:] Error: " + ex.getMessage());
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
            CTLocation targetLocation = new CTLocation(
                    result.getString("target_server"),
                    result.getString("target_world"),
                    result.getDouble("target_x"),
                    result.getDouble("target_y"),
                    result.getDouble("target_z"));

            portal = new Portal(
                    result.getString("name"),
                    Portal.PortalType.valueOf(result.getString("type")),
                    result.getInt("id"),
                    result.getString("target_host"),
                    result.getInt("target_port"),
                    targetLocation);
        }
        catch (Exception err) {
            plugin.getLogger().warning("[MySQL:] Error: " + err.getMessage());
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
            if (portal.getId().equals(id)) return portal;

        return null;
    }

    public Portal getPortal(Location location) {
        for (Portal portal : portals.values()) {
            if (!portal.getTargetLocation().getServer().equals(CraftBahnPlugin.plugin.getServerName())) continue;
            if (portal.getTargetLocation().getBukkitLocation().equals(location)) return portal;
        }

        return null;
    }
}