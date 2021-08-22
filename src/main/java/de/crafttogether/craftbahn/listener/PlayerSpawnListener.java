package de.crafttogether.craftbahn.listener;

import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        // Look if player should be a passenger
        Passenger passenger = Passenger.get(e.getPlayer().getUniqueId());

        // This player is not our passenger
        if (passenger != null)
            PortalHandler.reEnterPassenger(passenger, e);
    }
}

