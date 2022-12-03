package de.crafttogether.craftbahn.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Quoted;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

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
        cloud.suggest("destinationName", (context, input) -> {
            List<String> result =  plugin.getDestinationStorage().getDestinations().stream()
                    .map(Destination::getName)
                    .collect(Collectors.toList());
            return result;
        });


        // Register Annotations
        cloud.annotations(this);
        cloud.annotations(commands_destination);
    }

    @CommandMethod("fahrziel <name>")
    @CommandDescription("Setzt dem aktuell ausgewähltem Zug ein Fahrziel.")
    public void fahrziel(final CraftBahnPlugin plugin, final CommandSender sender, final @Argument(value="name", suggestions="destinationName") String name) {
        sender.sendMessage("There are " + plugin.getDestinationStorage().getDestinations().size() + " Destinations");
    }

    @CommandMethod("craftbahn")
    @CommandDescription("Zeigt die aktuelle Version des Plugin")
    public void craftbahn(final CraftBahnPlugin plugin, final CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "CraftBahn-Version: " + plugin.getDescription().getVersion());
    }
}