package de.crafttogether.craftbahn.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.List;

public class Message {

    public static TextComponent format(String message) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static BaseComponent newLine() {
        return (BaseComponent) new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
    }

    public void broadcast(List<Player> players, String message) {
        for (Player p : players)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
