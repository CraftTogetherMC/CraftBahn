package de.crafttogether.craftbahn.commands;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListCommand implements TabExecutor {
    private CraftBahn plugin = CraftBahn.getInstance();

    public boolean onCommand(CommandSender sender, Command cmd, String st, String[] args) {
        Player p = null;

        if (cmd.getName().equalsIgnoreCase("fahrziele")) {
            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player) sender).getUniqueId());

            else {
                Bukkit.getLogger().info("Dieser Befehl kann nicht von der Konsole ausgeführt werden.");
                return true;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}