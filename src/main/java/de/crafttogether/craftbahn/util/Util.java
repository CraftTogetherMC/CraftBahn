package de.crafttogether.craftbahn.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Util {
    public static OfflinePlayer getOfflinePlayer(String name) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                .distinct()
                .filter(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList()).get(0);
    }
}
