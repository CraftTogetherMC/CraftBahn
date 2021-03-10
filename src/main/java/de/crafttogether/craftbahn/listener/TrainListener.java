package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class TrainListener implements Listener {
    public List<Player> drivingPlayers = new ArrayList<Player>();

    public TrainListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player))
            return;

        if (!(e.getVehicle() instanceof Minecart))
            return;

        Player p = (Player)e.getEntered();
        Minecart minecart = (Minecart)e.getVehicle();
        MinecartMember<?> cart = MinecartMemberStore.getFromEntity((Entity)minecart);

        if (cart == null)
            return;
    }

    @EventHandler
    void onVehicleExit(VehicleExitEvent e) {

    }
}
