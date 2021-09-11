package de.crafttogether.craftbahn.tasks;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.SpeedData;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;

public class Speedometer implements Runnable {
    private LinkedList<SpeedData> trains;
    private BukkitTask task;

    public Speedometer() {
        this.trains = new LinkedList<>();
        this.task = Bukkit.getScheduler().runTaskTimer(CraftBahnPlugin.getInstance(), this, 20L, 5L);
    }

    @Override
    public void run() {
        updateData();
        sendActionBars();
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

    public void add(MinecartGroup train) {
        if (!exists(train))
            trains.add(new SpeedData(train));
    }

    public boolean exists(MinecartGroup train) {
        if (trains.contains(train))
            return true;

        return false;
    }

    public void remove(MinecartGroup train) {
        TCHelper.sendActionbar(train, "");
        trains.remove(train);
    }

    public void sendActionBars() {
        for (SpeedData data : trains) {
            MinecartGroup train = data.getTrain();
            String destinationName = data.getDestinationName();
            double velocity = data.getVelocity();
            double distance = data.getDistance();

            if (velocity > 0) {
                int minuten = 0;
                int sekunden = 0;
                int time = (int) (distance/velocity);

                sekunden = time % 60;
                minuten = (time-sekunden)/60;

                if (distance > 5) {
                    if (time > 3)
                        TCHelper.sendActionbar(train, String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"%s\" §8| §6ETA: §e%d:%02d", velocity, distance, destinationName, minuten, sekunden));
                    else
                        TCHelper.sendActionbar(train, String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"%s\"", velocity, distance, destinationName));
                }

                else
                    TCHelper.sendActionbar(train, String.format("§e%.1f §6Blöcke/s", velocity));
            }

            else if (distance > 5)
                TCHelper.sendActionbar(train, String.format("§e%.0f §6Blöcke bis \"%s\"", distance, destinationName));
            else
                TCHelper.sendActionbar(train, "");
        }
    }

    private void updateData() {
        for (SpeedData data : trains)
            data.update();
    }

}
