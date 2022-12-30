package de.crafttogether.craftbahn.listener;

import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {
        // Look if entity should be a passenger
        Passenger passenger = Passenger.get(event.getEntity().getUniqueId());

        if (passenger != null)
            PortalHandler.reEnterEntity(passenger, event);
    }
}
