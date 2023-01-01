package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.speedometer.Speedometer;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class TrainEnterListener implements Listener {

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        Speedometer speedometer = CraftBahnPlugin.plugin.getSpeedometer();

        if (!(e.getEntered() instanceof Player) || speedometer == null)
            return;

        MinecartMember<?> member = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (member == null)
            return;

        String destinationInfo;
        String destination = member.getGroup().getProperties().getDestination();

        // Show enhanced message if train is tagged as 'craftbahn'
        if (TCHelper.hasTagIgnoreCase("craftbahn", member.getGroup().getProperties().getTags())) {
            if (destination.isEmpty())
                destinationInfo = Localization.ENTERMESSAGE_NODEST.get();
            else
                destinationInfo = Localization.ENTERMESSAGE_DEST.get();

           Localization.ENTERMESSAGE.message(e.getEntered(),
                    PlaceholderResolver.resolver("destinationInfo", destinationInfo),
                    PlaceholderResolver.resolver("destination", destination));
        }

        // Add Speedometer for train if no one exists
        String trainName = member.getGroup().getProperties().getTrainName();
        if (speedometer.get(trainName) == null)
            speedometer.add(trainName);
    }
}