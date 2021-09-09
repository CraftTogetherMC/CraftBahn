package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.pathfinding.PathConnection;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.PathProvider;
import de.crafttogether.CraftBahnPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;

public class Speedometer implements Runnable {
    private LinkedList<Player> players;
    private BukkitTask task;

    public Speedometer() {
        this.players = new LinkedList<>();
        this.task = Bukkit.getScheduler().runTaskTimer(CraftBahnPlugin.getInstance(), this, 20L, 5L);
    }

    @Override
    public void run() {
        sendActionBars();
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

    public void add(Player p) {
        players.add(p);
    }

    public void remove(Player p) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        players.remove(p);
    }

    public void sendActionBars() {
        for(Player p : players){
            double velocity = calcVelocity(p);

            if (velocity > 0)
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("§e%.3f §6Blöcke/s", velocity)));
            else
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
    }

    private double calcVelocity(Player p) {
        //Vector v = TCHelper.getTrain(p).head().getRealSpeed();
        //return Math.sqrt(v.getX()*v.getX() + v.getY()*v.getY() + v.getZ()*v.getZ())*20;

        MinecartGroup train = TCHelper.getTrain(p);
        Block rail = train.head().getRailTracker().getLastBlock();
        String destination = train.getProperties().getDestination();
        if (destination != null) {
            PathProvider provider = TrainCarts.plugin.getPathProvider();
            PathNode node = provider.getWorld(rail.getWorld()).getNodeAtRail(rail);

            if (node != null) {
                PathConnection connection = node.findConnection(destination);

                if (connection != null) {
                    Message.debug(p, "Noch " + connection.distance + " Blöcke bis zum Ziel!");
                    Message.debug(p.getName() + " Hat noch " + connection.distance + " Blöcke bis zum Ziel!");
                }
            }
        }

        return train.head().getRealSpeedLimited()*20;
    }
}
