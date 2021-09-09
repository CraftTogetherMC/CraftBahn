package de.crafttogether.craftbahn.util;

import de.crafttogether.CraftBahnPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedList;

public class Speedometer {
    private LinkedList<Player> players;

    public Speedometer() {
        this.players = new LinkedList<>();

        run();
    }

    public void add(Player p){
        players.add(p);
    }

    public void remove(Player p){
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        players.remove(p);
    }

    public void sendActionBars(){
        for(Player p : players){
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN +
                    String.format("%.3f Blöcke/s",calcVelocity(p))));
        }
    }

    private void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                sendActionBars();
            }
        }.runTaskTimer(CraftBahnPlugin.getInstance(), 20, 5);
    }

    private double calcVelocity(Player p){
        //Vector v = TCHelper.getTrain(p).head().getRealSpeed();
        //return Math.sqrt(v.getX()*v.getX() + v.getY()*v.getY() + v.getZ()*v.getZ())*20;
        return TCHelper.getTrain(p).head().getRealSpeedLimited()*20;
    }
}
