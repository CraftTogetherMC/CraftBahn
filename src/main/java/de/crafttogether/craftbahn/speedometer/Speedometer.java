package de.crafttogether.craftbahn.speedometer;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.TCHelper;
import de.crafttogether.craftbahn.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Speedometer implements Runnable {
    private LinkedList<SpeedData> trains;
    private HashMap<String, Location> particleLocations;
    private BukkitTask task;

    public Speedometer() {
        this.trains = new LinkedList<>();
        this.particleLocations = new HashMap<>();
        this.task = Bukkit.getScheduler().runTaskTimer(CraftBahnPlugin.plugin, this, 20L, 5L);
    }

    @Override
    public void run() {
        if (trains.size() < 1)
            return;

        updateData();
        sendActionBars();

        if (particleLocations.values().size() < 1)
            return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("craftbahn.debug")) continue;

            for (Location particleLocation : particleLocations.values()) {
                if (particleLocation.getChunk().isLoaded())
                    p.spawnParticle(Particle.BLOCK_MARKER, particleLocation, 1, Material.BARRIER.createBlockData());
            }
        }
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

    public void add(String trainName) {
        if (get(trainName) != null) return;
        trains.add(new SpeedData(trainName));
    }

    public SpeedData get(String trainName) {
        List<SpeedData> data = trains.stream()
                .filter(speedData -> speedData.getTrainName().equals(trainName))
                .distinct()
                .toList();

        if (data.size() < 1)
            return null;

        return data.get(0);
    }

    public void remove(String trainName) {
        SpeedData data = get(trainName);
        if (data == null) return;

        // Clear actionbar for all players
        MinecartGroup train = TCHelper.getTrain(trainName);
        TCHelper.sendActionbar(train, Component.text(""));

        Util.debug("REMOVE SPEEDOMETER FOR TRAIN: " + train.getProperties().getTrainName());
        particleLocations.remove(train.getProperties().getTrainName());
        trains.remove(data);
    }

    public void sendActionBars() {
        for (SpeedData data : trains) {
            MinecartGroup group = TCHelper.getTrain(data.getTrainName());

            Component message;
            String destinationName = data.getDestinationName();

            double realVelocity = data.getRealVelocity();
            double smoothedVelocity = data.getSmoothVelocity();
            double distance = data.getDistance();

            if (realVelocity > 0) {
                int minuten, sekunden;
                int time = (int) (distance / smoothedVelocity);

                sekunden = time % 60;
                minuten = (time-sekunden) / 60;

                if (distance > 5) {
                    if (time > 3)
                        message = Component.text(String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\" §8| §6ETA: §e%d:%02d", realVelocity, distance, destinationName, minuten, sekunden));
                    else
                        message = Component.text(String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\"", realVelocity, distance, destinationName));
                }

                else
                    message = Component.text(String.format("§e%.1f §6Blöcke/s", realVelocity));
            }

            else if (distance > 5)
                message = Component.text(String.format("§e%.0f §6Blöcke bis \"%s\"", distance, destinationName));
            else
                message = Component.text("");

            TCHelper.sendActionbar(group, "craftbahn.speedometer", message);
        }
    }

    private void updateData() {
        for (SpeedData data : trains)
            data.update();
    }

    public HashMap<String, Location> getParticleLocations() {
        return particleLocations;
    }
}