package de.crafttogether.craftbahn.util;

import de.crafttogether.craftbahn.CraftBahn;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class CTLocation {
    private String server;
    private String world;
    private double x;
    private double y;
    private double z;

    public CTLocation(String server, String world, double x, double y, double z) {
        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location getBukkitLocation() {
        if (Bukkit.getWorld(this.world) == null)
            return null;

        return new Location(Bukkit.getWorld(this.world), this.getX(), this.getY(), this.getZ());
    }

    public static CTLocation fromBukkitLocation(Location loc) {
        return new CTLocation(CraftBahn.getInstance().getServerName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    public String getServer() {
        return server;
    }
    public String getWorld() {
        return world;
    }
    public double getX() {
        return x;
    }
    public double getY() { return y; }
    public double getZ() {
        return z;
    }

    public void setServer(String server) {
        this.server = server;
    }
    public void setWorld(String world) {
        this.world = world;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public String toString() {
        return "server=" + server + ", world=" + world + ", x=" + x + ", y=" + y + ", z=" + z;
    }
}
