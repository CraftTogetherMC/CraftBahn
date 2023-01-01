package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.speedometer.Speedometer;
import de.crafttogether.craftbahn.util.TCHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class TrainExitListener implements Listener {

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        Speedometer speedometer = CraftBahnPlugin.plugin.getSpeedometer();

        if (!(e.getExited() instanceof Player player) || speedometer == null)
            return;

        MinecartMember<?> member = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (member == null)
            return;

        // Check if train has no more passengers
        if (TCHelper.getPlayerPassengers(member.getGroup()).size() <= 1) {
            // Remove Speedometer if activated
            speedometer.remove(member.getGroup().getProperties().getTrainName());
        }

        // Clear ActionBar
        player.sendActionBar(Component.text(""));
    }
}
