package de.crafttogether.craftbahn.commands;

import cloud.commandframework.annotations.*;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationList;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.util.TCHelper;
import de.crafttogether.craftbahn.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DestinationCommands {
    private final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;
    private CloudSimpleHandler cloud = null;

    public DestinationCommands(CloudSimpleHandler cloud) {
        this.cloud = cloud;
    }

    @CommandMethod(value="fahrziel", requiredSender=Player.class)
    @CommandDescription("Informationen zum /fahrziel Befehl")
    @CommandPermission("craftbahn.command.destination")
    public void fahrziel_info(
            final Player sender
    ) {
        Localization.COMMAND_DESTINATION_INFO.message(sender);
    }

    @CommandMethod(value="fahrziel <name> [server]", requiredSender=Player.class)
    @CommandDescription("Setzt dem aktuell ausgewähltem Zug ein Fahrziel.")
    @CommandPermission("craftbahn.command.destination")
    public void fahrziel(
            final Player sender,
            final @Argument(value="name", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server,
            final @Flag(value="page") Integer page
    ) {
        ArrayList<Destination> result = new ArrayList<>();
        if (server == null || server.isEmpty())
            result = new ArrayList<>(plugin.getDestinationStorage().getDestinations(name));
        else
            result.add(plugin.getDestinationStorage().getDestination(name, server));

        // No destination was found
        if (result.size() < 1 || result.get(0) == null) {
            Localization.COMMAND_DESTINATION_NOTEXIST.message(sender,
                PlaceholderResolver.resolver("input", name)
            );
        }

        // Multiple destinations have been found
        else if (result.size() > 1) {
            DestinationList list = new DestinationList(result);
            list.setCommand("/fahrziel");
            list.setRowsPerPage(12);
            list.showContentsPage(false);
            list.showFooterLine(true);
            list.showOwner(true);
            list.showLocation(true);

            if (!LogicUtil.nullOrEmpty(Localization.HEADER.get()))
                sender.sendMessage(Localization.HEADER.deserialize().append(Component.newline()));

            if (page == null) {
                Localization.COMMAND_DESTINATION_MULTIPLEDEST.message(sender);
            }

            list.sendPage(sender, (page == null ? 1 : page));
        }

        // A single destination was found
        else {
            Destination dest = result.get(0);

            // Check permission and if destination is public
            if (!dest.isPublic() && !sender.hasPermission("craftbahn.destination.see.private")) {
                Localization.COMMAND_DESTINATION_NOPERMISSION.message(sender);
                return;
            }

            // Find train
            MinecartGroup train = TCHelper.getTrain(sender);
            if (train == null) {
                Localization.COMMAND_NOTRAIN.message(sender);
                return;
            }

            // Apply destination
            train.getProperties().setDestination("train destination " + dest.getServer());

            if (!CraftBahnPlugin.plugin.getServerName().equalsIgnoreCase(dest.getServer())) {
                // Create a route first
                List<String> route = new ArrayList<>();
                route.add(dest.getServer());
                route.add(dest.getName());

                train.getProperties().setDestinationRoute(route);
                train.getProperties().setDestination(dest.getServer());
            } else {
                train.getProperties().setDestination(dest.getName());
            }

            Localization.COMMAND_DESTINATION_APPLIED.message(sender,
                PlaceholderResolver.resolver("destination", dest.getName())
            );
        }
    }

    @CommandMethod(value="fahrziele [type]", requiredSender=Player.class)
    @CommandDescription("Zeigt eine Liste mit möglichen Fahrzielen.")
    @CommandPermission("craftbahn.command.destination")
    public void fahrziele(
            final Player sender,
            final @Argument(value="type", suggestions="destinationType") String type,
            final @Flag(value="player", suggestions="onlinePlayers", permission = "craftbahn.command.destination.filter") String player,
            final @Flag(value="server", suggestions="serverName", permission = "craftbahn.command.destination.filter") String server,
            final @Flag(value="page") Integer page
    ) {
        List<Destination> result = new ArrayList<>(CraftBahnPlugin.plugin.getDestinationStorage().getDestinations());
        String commandFlags = "";

        // Filter: stationType
        if (type != null && !type.isEmpty()) {
            commandFlags = type;
            result = result.stream()
                    .filter(d -> d.getType().toString().equalsIgnoreCase(type))
                    .toList();
        }

        // Filter: player
        if (player != null && !player.isEmpty()) {
            if (Util.getOfflinePlayer(player) == null) {
                return;
            }

            commandFlags += "--player " + player;
            result = result.stream()
                    .filter(d -> d.getOwner().equals(player))
                    .filter(d -> d.getParticipants().equals(player))
                    .toList();
        }

        // Filter: server
        if (server != null && !server.isEmpty()) {
            commandFlags += "--server " + server;
            result = result.stream()
                    .filter(d -> d.getServer().equalsIgnoreCase(server))
                    .toList();
        }

        if (result.size() < 1) {
            Localization.COMMAND_DESTINATIONS_LIST_EMPTY.message(sender);
            return;
        }

        DestinationList list = new DestinationList(result);
        list.setCommand("/fahrziele");
        list.setCommandFlags(commandFlags);
        list.showContentsPage(commandFlags.isEmpty() ? true : false);
        list.showFooterLine(true);
        list.showOwner(true);
        list.showLocation(true);

        list.sendPage(sender, (page == null ? 1 : page));
    }

    @CommandMethod(value="fahrzieledit setowner", requiredSender=Player.class)
    @CommandDescription("Ändert den angegebenen Besitzer eines Fahrziel")
    @CommandPermission("craftbahn.command.destination.setowner")
    public void fahrzieledit_setowner(
            final Player sender
    ) {
        sender.sendMessage("Test");
    }
}
