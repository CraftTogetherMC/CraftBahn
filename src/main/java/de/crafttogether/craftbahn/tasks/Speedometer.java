package de.crafttogether.craftbahn.tasks;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Message;
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
        if (get(train) != null) return;

        Message.debug("ADD SPEEDOMETER FOR TRAIN: " + train.getProperties().getTrainName());
        trains.add(new SpeedData(train));
    }

    public SpeedData get(MinecartGroup train) {
        SpeedData speedData = null;

        for (SpeedData data : trains) {
            if (data.getTrain().getProperties().getTrainName().equals(train.getProperties().getTrainName()))
                speedData = data;
        }

        return speedData;
    }

    public void remove(MinecartGroup train) {
        SpeedData data = get(train);
        if (data == null) return;

        // Clear actionbar for all players
        TCHelper.sendActionbar(train, "");

        Message.debug("REMOVE SPEEDOMETER FOR TRAIN: " + train.getProperties().getTrainName());
        trains.remove(data);
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
                        TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"%s\" §8| §6ETA: §e%d:%02d", velocity, distance, destinationName, minuten, sekunden));
                    else
                        TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"%s\"", velocity, distance, destinationName));
                }

                else
                    TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.1f §6Blöcke/s", velocity));
            }

            else if (distance > 5)
                TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.0f §6Blöcke bis \"%s\"", distance, destinationName));
            else
                TCHelper.sendActionbar(train, "craftbahn.speedometer", "");
        }
    }

    private void updateData() {
        for (SpeedData data : trains)
            data.update();
    }

}
