package de.crafttogether.craftbahn.listener;

import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerJoinEvent e) {
        Message.debug("#PlayerJoinEvent");

        // Look if player should be a passenger
        Passenger passenger = Passenger.get(e.getPlayer().getUniqueId());

        // This player is not our passenger
        if (passenger != null)
            PortalHandler.reEnterPassenger(passenger, e);
    }
}

