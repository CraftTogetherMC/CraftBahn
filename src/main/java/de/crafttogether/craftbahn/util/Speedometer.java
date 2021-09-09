package de.crafttogether.craftbahn.util;

import de.crafttogether.CraftBahnPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("§e%.3f §6Blöcke/s", calcVelocity(p))));
        }
    }

    private double calcVelocity(Player p) {
        //Vector v = TCHelper.getTrain(p).head().getRealSpeed();
        //return Math.sqrt(v.getX()*v.getX() + v.getY()*v.getY() + v.getZ()*v.getZ())*20;
        return TCHelper.getTrain(p).head().getRealSpeedLimited()*20;
    }
}
