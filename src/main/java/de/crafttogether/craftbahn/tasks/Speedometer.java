package de.crafttogether.craftbahn.tasks;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.pathfinding.PathConnection;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.PathProvider;
import com.bergerkiller.bukkit.tc.pathfinding.PathRailInfo;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import com.bergerkiller.bukkit.tc.utils.TrackWalkingPoint;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.SpeedData;
import de.crafttogether.craftbahn.util.TCHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;

public class Speedometer implements Runnable {
    private LinkedList<SpeedData> players;
    private BukkitTask task;

    public Speedometer() {
        this.players = new LinkedList<>();
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

    public void add(Player p) {
        players.add(new SpeedData(p));
    }

    public void remove(Player p) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        for (SpeedData data : players) {
            if (data.getPlayer() == p) {
                players.remove(data);
                break;
            }
        }
    }

    public void sendActionBars() {
        for (SpeedData data : players) {
            Player p = data.getPlayer();
            double velocity = data.getVelocity();
            double distance = data.getDistance();
            String destinationName = data.getDestinationName();
            if (velocity > 0) {
                if (distance > 5) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("§e%.3f §6Blöcke/s §8| §e%.1f §6Blöcke bis \"%s\"", velocity, distance, destinationName)));
                } else {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("§e%.3f §6Blöcke/s", velocity)));
                }
            } else if (distance > 5) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("§e%.1f §6Blöcke bis \"%s\"", distance, destinationName)));
            } else {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
            }
        }
    }

    private void updateData() {
        for (SpeedData data : players) {
            data.update();
        }
    }

}
