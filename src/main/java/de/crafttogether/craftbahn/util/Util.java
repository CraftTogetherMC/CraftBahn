package de.crafttogether.craftbahn.util;

import de.crafttogether.CraftBahnPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

public class Util {
    public static OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .distinct()
                .filter(offlinePlayer -> Objects.requireNonNull(offlinePlayer.getName()).equalsIgnoreCase(name)).toList().get(0);
    }

    public static void debug(Component message, boolean broadcast) {
        message = LegacyComponentSerializer.legacyAmpersand().deserialize("<gray><bold>[Debug]: </bold></gray><reset>").append(message);

        // Broadcast to online players with permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (CraftBahnPlugin.plugin.getConfig().getBoolean("Settings.Debug")) continue;
            player.sendMessage(message);
        }

        CraftBahnPlugin.plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
    }

    public static void debug(String message, boolean broadcast) {
        debug(Component.text(message), broadcast);
    }
    public static void debug(String message) {
        debug(Component.text(message), false);
    }
    public static void debug(Component message) {
        debug(message, false);
    }
}
