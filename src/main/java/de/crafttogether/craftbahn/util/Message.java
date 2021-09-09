package de.crafttogether.craftbahn.util;

import de.crafttogether.CraftBahnPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class Message {

    public static TextComponent format(String message) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static BaseComponent newLine() {
        return new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
    }

    public void broadcast(List<Player> players, String message) {
        for (Player p : players)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void debug(Player p, String message) {
        if (!CraftBahnPlugin.getInstance().getConfig().getBoolean("Settings.Debug") || !p.hasPermission("craftbahn.debug")) return;
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&l[Debug]: &e" + message));
    }

    public static void debug(String message) {
        //if (CraftBahnPlugin.getInstance().getConfig().getBoolean("debug"))
        CraftBahnPlugin.getInstance().getLogger().info("[Debug]: " + message);
    }
}
