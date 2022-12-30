package de.crafttogether.craftbahn.listener;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        // Look if entity should be a passenger
        Passenger passenger = Passenger.get(event.getEntity().getUniqueId());

        if (passenger != null)
            PortalHandler.reEnterEntity(passenger, event);
    }

    @EventHandler
    public void onDie(EntityDeathEvent event) {
        if (event.getEntity().getType().equals(EntityType.VILLAGER)) {
            Location loc = event.getEntity().getLocation();
            CraftBahnPlugin.plugin.getLogger().warning("Villager died at " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
        }
    }
}
