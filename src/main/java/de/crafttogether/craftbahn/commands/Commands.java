package de.crafttogether.craftbahn.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {
    private static final CloudSimpleHandler cloud = new CloudSimpleHandler();

    public void enable(CraftBahnPlugin plugin) {
        cloud.enable(plugin);

        // Command handlers
        DestinationCommands commands_destination = new DestinationCommands(cloud);

        // Suggestions
        cloud.suggest("onlinePlayers", (context, input) -> {
            List<String> result = Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return result;
        });

        cloud.suggest("serverName", (context, input) -> {
            List<String> result =  plugin.getDestinationStorage().getDestinations().stream().distinct()
                    .map(Destination::getServer)
                    .collect(Collectors.toList());
            return result;
        });

        cloud.suggest("destinationName", (context, input) -> {
            List<String> result =  plugin.getDestinationStorage().getDestinations().stream()
                    .map(Destination::getName)
                    .collect(Collectors.toList());
            return result;
        });

        cloud.suggest("destinationType", (context, input) -> {
            List<String> result =  Arrays.stream(Destination.DestinationType.values())
                    .map(Destination.DestinationType::name)
                    .collect(Collectors.toList());
            return result;
        });

        // Register Annotations
        cloud.annotations(this);
        cloud.annotations(commands_destination);
    }

    @CommandMethod("craftbahn")
    @CommandDescription("Zeigt die aktuelle Version des Plugin")
    public void craftbahn(final CraftBahnPlugin plugin, final CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "CraftBahn-Version: " + plugin.getDescription().getVersion());
    }

    public static CloudSimpleHandler getCloud() {
        return cloud;
    }

    public CommandManager getManager() {
        return cloud.getManager();
    }
}