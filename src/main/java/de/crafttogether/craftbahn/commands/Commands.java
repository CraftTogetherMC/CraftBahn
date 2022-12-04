package de.crafttogether.craftbahn.commands;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {
    private final CloudSimpleHandler cloud = new CloudSimpleHandler();

    // Command handlers
    private final DestinationCommands commands_destination = new DestinationCommands();

    public CloudSimpleHandler getHandler() {
        return cloud;
    }

    public void enable(CraftBahnPlugin plugin) {
        cloud.enable(plugin);

        // Suggestions
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
}