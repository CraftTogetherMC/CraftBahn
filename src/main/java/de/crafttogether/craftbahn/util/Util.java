package de.crafttogether.craftbahn.util;

import com.google.common.io.ByteStreams;
import de.crafttogether.CraftBahnPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class Util {
    public static OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .distinct()
                .filter(offlinePlayer -> Objects.requireNonNull(offlinePlayer.getName()).equalsIgnoreCase(name)).toList().get(0);
    }

    public static void exportResource(String resourcePath) {
        CraftBahnPlugin plugin = CraftBahnPlugin.plugin;

        File inputFile = new File(resourcePath);
        File outputFile = new File(plugin.getDataFolder() + File.separator + inputFile.getName());

        if (outputFile.exists())
            return;

        if (!inputFile.exists()) {
            plugin.getLogger().warning("Could not find resource '" + resourcePath + "'");
            return;
        }

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();

        try {
            outputFile.createNewFile();
            InputStream inputStream = CraftBahnPlugin.plugin.getResource(resourcePath);
            OutputStream os = new FileOutputStream(outputFile);
            ByteStreams.copy(inputStream, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void debug(String message, boolean broadcast) {
        Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l[Debug]: &r" + message);

        // Broadcast to online players with permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (CraftBahnPlugin.plugin.getConfig().getBoolean("Settings.Debug")) continue;
            player.sendMessage(messageComponent);
        }

        CraftBahnPlugin.plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(messageComponent));
    }

    public static void debug(String message) {
        debug(Component.text(message), false);
    }
    public static void debug(Component message, boolean broadcast) {
        debug(LegacyComponentSerializer.legacyAmpersand().serialize(message), broadcast);
    }
    public static void debug(Component message) {
        debug(message, false);
    }
}
