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

    public static void openBook(ItemStack book, Player p) {
        int slot = p.getInventory().getHeldItemSlot();
        ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte)0);
        buf.writerIndex(1);

        try {
            Constructor<?> serializerConstructor = NMSUtils.getNMSClass("PacketDataSerializer").getConstructor(ByteBuf.class);
            Object packetDataSerializer = serializerConstructor.newInstance(buf);

            Constructor<?> keyConstructor = NMSUtils.getNMSClass("MinecraftKey").getConstructor(String.class);
            Object bookKey = keyConstructor.newInstance("minecraft:book_open");

            Constructor<?> titleConstructor = NMSUtils.getNMSClass("PacketPlayOutCustomPayload").getConstructor(bookKey.getClass(), NMSUtils.getNMSClass("PacketDataSerializer"));
            Object payload = titleConstructor.newInstance(bookKey, packetDataSerializer);

            NMSUtils.sendPacket(p, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }

        p.getInventory().setItem(slot, old);
    }
}
