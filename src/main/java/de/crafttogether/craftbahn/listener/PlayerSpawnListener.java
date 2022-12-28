package de.crafttogether.craftbahn.listener;

import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        // Look if player should be a passenger
        Passenger passenger = Passenger.get(event.getPlayer().getUniqueId());

        if (passenger != null) {
            if (passenger.hasError()) {
                event.getPlayer().sendMessage(passenger.getError());
            }

            else {
                if (event.getPlayer().isFlying())
                    event.getPlayer().setFlying(false);

                PortalHandler.reEnterPassenger(passenger, event);
            }
        }
    }
}