package de.crafttogether.craftbahn.tasks;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.SpeedData;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedList;

public class Speedometer implements Runnable {
    private LinkedList<SpeedData> trains;
    public HashMap<String, Location> markerParticles;
    private BukkitTask task;

    public Speedometer() {
        this.markerParticles = new HashMap<>();
        this.trains = new LinkedList<>();
        this.task = Bukkit.getScheduler().runTaskTimer(CraftBahnPlugin.getInstance(), this, 20L, 5L);
    }

    @Override
    public void run() {
        updateData();
        sendActionBars();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("craftbahn.debug")) continue;

            for (Location particleLocation : markerParticles.values()) {
                if (particleLocation.getChunk().isLoaded())
                    p.spawnParticle(Particle.BARRIER, particleLocation, 1);
            }
        }
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

    public void add(String trainName) {
        if (get(trainName) != null) return;

        Message.debug("ADD SPEEDOMETER FOR TRAIN: " + trainName);
        try {
            trains.add(new SpeedData(trainName));
        }
        catch (Exception ignore) { }
    }

    public SpeedData get(String trainName) {
        SpeedData speedData = null;

        for (SpeedData data : trains) {
            if (data.getTrainName().equals(trainName))
                speedData = data;
        }

        return speedData;
    }

    public void remove(String trainName) {
        SpeedData data = get(trainName);
        if (data == null) return;

        // Clear actionbar for all players
        MinecartGroup train = TCHelper.getTrain(trainName);
        TCHelper.sendActionbar(train, "");

        Message.debug("REMOVE SPEEDOMETER FOR TRAIN: " + train.getProperties().getTrainName());
        markerParticles.remove(train.getProperties().getTrainName());
        trains.remove(data);
    }

    public void sendActionBars() {
        for (SpeedData data : trains) {
            MinecartGroup train = TCHelper.getTrain(data.getTrainName());
            String destinationName = data.getDestinationName();
            double realVelocity = data.getRealVelocity();
            double smoothedVelocity = data.getSmoothVelocity();
            double distance = data.getDistance();

            if (realVelocity > 0) {
                int minuten = 0;
                int sekunden = 0;
                int time = (int) (distance/smoothedVelocity);

                sekunden = time % 60;
                minuten = (time-sekunden)/60;

                if (distance > 5) {
                    if (time > 3)
                        TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\" §8| §6ETA: §e%d:%02d", realVelocity, distance, destinationName, minuten, sekunden));
                    else
                        TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\"", realVelocity, distance, destinationName));
                }

                else
                    TCHelper.sendActionbar(train, "craftbahn.speedometer", String.format("§e%.1f §6Blöcke/s", realVelocity));
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
