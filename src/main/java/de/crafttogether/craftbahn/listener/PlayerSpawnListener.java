package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.craftbahn.portals.Passenger;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Message.debug(e.getPlayer().getName() + " -> #joinEvent");

        // Look if player should be a passenger
        Passenger passenger = Passenger.get(e.getPlayer().getUniqueId());

        Message.debug(e.getPlayer().getName() + " -> Passenger Check...");
        if (passenger != null)
            PortalHandler.reEnterPassenger(passenger, e);
        else {
            Message.debug(e.getPlayer().getName() + " -> Is not a passenger");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent e) {
        Message.debug(e.getPlayer().getName() + " -> #spawnLocationEvent");

        // Spawn player at correct location
        Passenger passenger = Passenger.get(e.getPlayer().getUniqueId());

        Message.debug(e.getPlayer().getName() + " -> Passenger Check...");
        if (passenger != null) {
            Message.debug(e.getPlayer().getName() + " -> Try to find a train");

            // Try to find train and set player as passenger
            MinecartGroup train = PortalHandler.getSpawnedTrain(passenger.getTrainId());

            if (train == null) {
                Message.debug(e.getPlayer().getName() + " -> Train #" + passenger.getTrainId() + " not found");
                return;
            }

            Message.debug(e.getPlayer().getName() + " -> SpawnLocation set!");
            e.setSpawnLocation(train.get(passenger.getCartIndex()).getEntity().getLocation());
        }
        else
            Message.debug(e.getPlayer().getName() + " -> Is not a passenger");
    }
}

