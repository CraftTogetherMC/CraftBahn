package de.crafttogether.craftbahn.util;

import de.crafttogether.CraftBahnPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Message {
    public static Component deserialize(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static void debug(Player p, String message) {
        if (!CraftBahnPlugin.plugin.getConfig().getBoolean("debug") || !p.hasPermission("craftbahn.debug")) return;
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&l[Debug]: &e" + message));
    }

    public static void debug(String message) {
        //if (CraftBahnPlugin.getInstance().getConfig().getBoolean("debug"))
        CraftBahnPlugin.plugin.getLogger().info("[Debug]: " + message);
    }
}