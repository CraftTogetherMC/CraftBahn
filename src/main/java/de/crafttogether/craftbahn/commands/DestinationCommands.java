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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @CommandMethod(value="fahrziel <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Setzt dem aktuell ausgewähltem Zug ein Fahrziel")
    @CommandPermission("craftbahn.command.destination")
    public void fahrziel(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
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
            Destination destination = result.get(0);

            // Check permission and if destination is public
            if (!destination.isPublic() && !sender.hasPermission("craftbahn.destination.see.private")) {
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
            train.getProperties().setDestination("train destination " + destination.getServer());

            if (!CraftBahnPlugin.plugin.getServerName().equalsIgnoreCase(destination.getServer())) {
                // Create a route first
                List<String> route = new ArrayList<>();
                route.add(destination.getServer());
                route.add(destination.getName());

                train.getProperties().setDestinationRoute(route);
                train.getProperties().setDestination(destination.getServer());
            } else {
                train.getProperties().setDestination(destination.getName());
            }

            Localization.COMMAND_DESTINATION_APPLIED.message(sender,
                PlaceholderResolver.resolver("destination", destination.getName())
            );
        }
    }

    @CommandMethod(value="fahrziele [type]", requiredSender=Player.class)
    @CommandDescription("Zeigt eine Liste mit möglichen Fahrzielen")
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
        Boolean showContentsPage = true;

        // Filter: stationType
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase(Localization.DESTINATIONTYPE_ALL.get())) {
            commandFlags = " " + type;
            showContentsPage = false;
            result = result.stream()
                    .filter(d -> d.getType().toString().equalsIgnoreCase(type))
                    .toList();
        }
        else
            commandFlags = " " + Localization.DESTINATIONTYPE_ALL.get();

        // Filter: player
        if (player != null && !player.isEmpty()) {
            if (Util.getOfflinePlayer(player) == null) {
                return;
            }

            commandFlags += " --player " + player;
            showContentsPage = false;
            result = result.stream()
                    .filter(d -> d.getOwner().equals(player))
                    .filter(d -> d.getParticipants().equals(player))
                    .toList();
        }

        // Filter: server
        if (server != null && !server.isEmpty()) {
            commandFlags += " --server " + server;
            showContentsPage = false;
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
        list.showContentsPage(showContentsPage);
        list.showFooterLine(true);
        list.showOwner(true);
        list.showLocation(true);

        list.sendPage(sender, (page == null ? 1 : page));
    }

    @CommandMethod(value="fahrzieledit info <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Zeigt Informationen zum angegebenen Fahrziel an")
    @CommandPermission("craftbahn.command.destination.edit.info")
    public void fahrzieledit_info(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server,
            final @Flag(value="page") Integer page
    ) {
        if (LogicUtil.nullOrEmpty(name)) {
            Localization.COMMAND_DESTEDIT_NONAME.message(sender);
            return;
        }

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
            Localization.COMMAND_DESTEDIT_MULTIPLEDEST.message(sender);
        }

        // A single destination was found
        else {
            Destination destination = result.get(0);

            OfflinePlayer owner = Bukkit.getOfflinePlayer(destination.getOwner());
            String unkown = Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNERUNKOWN.get();
            String ownerName = (owner.hasPlayedBefore() ? owner.getName() : unkown);

            StringBuilder participants = new StringBuilder();
            for (UUID uuid : destination.getParticipants()) {
                OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                if (!participant.hasPlayedBefore()) continue;
                participants.append(participant.getName()).append(", ");
            }

            Localization.COMMAND_DESTEDIT_INFO.message(sender,
                    PlaceholderResolver.resolver("name", destination.getName()),
                    PlaceholderResolver.resolver("id", destination.getId().toString()),
                    PlaceholderResolver.resolver("type", destination.getType().toString()),
                    PlaceholderResolver.resolver("owner", ownerName),
                    PlaceholderResolver.resolver("participants", participants.isEmpty() ? "" : participants.substring(0, participants.length()-2)),
                    PlaceholderResolver.resolver("server", destination.getServer()),
                    PlaceholderResolver.resolver("world", destination.getWorld()),
                    PlaceholderResolver.resolver("x", String.valueOf(destination.getLocation().getX())),
                    PlaceholderResolver.resolver("y", String.valueOf(destination.getLocation().getY())),
                    PlaceholderResolver.resolver("z", String.valueOf(destination.getLocation().getZ())));
        }
    }

    @CommandMethod(value="fahrzieledit tp <destination>", requiredSender=Player.class)
    @CommandDescription("Teleportiert den Spieler zum angegebenen Fahrziel")
    @CommandPermission("craftbahn.command.destination.edit.teleport")
    public void fahrzieledit_teleport(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit add <destination>")
    @CommandDescription("Fügt ein neues Fahrziel der Liste hinzu")
    @CommandPermission("craftbahn.command.destination.edit.add")
    public void fahrzieledit_add(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit remove <destination>")
    @CommandDescription("Entfernt das angegebene Fahrziel aus der Liste")
    @CommandPermission("craftbahn.command.destination.edit.remove")
    public void fahrzieledit_remove(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit addmember <destination> <player>")
    @CommandDescription("Fügt dem angegebene Fahrziel einen sekundären Besitzer hinzu")
    @CommandPermission("craftbahn.command.destination.edit.addmember")
    public void fahrzieledit_addmember(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination,
            final @Argument(value="player", suggestions="onlinePlayers") String player
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit removemember <destination> <player>")
    @CommandDescription("Entfernt dem angegebene Fahrziel einen sekundären Besitzer")
    @CommandPermission("craftbahn.command.destination.edit.removemember")
    public void fahrzieledit_removemember(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination,
            final @Argument(value="player", suggestions="onlinePlayers") String player
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit setowner <destination> <player>")
    @CommandDescription("Legt den primären Besitzer des angegebenen Fahrziel fest")
    @CommandPermission("craftbahn.command.destination.edit.setowner")
    public void fahrzieledit_setowner(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination,
            final @Argument(value="player", suggestions="onlinePlayers") String player
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit setprivate <destination>")
    @CommandDescription("Macht das angegebene Fahrziel privat")
    @CommandPermission("craftbahn.command.destination.edit.setprivate")
    public void fahrzieledit_setprivate(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit setpublic <destination>")
    @CommandDescription("Macht das angegebene Fahrziel öffentlich")
    @CommandPermission("craftbahn.command.destination.edit.setpublic")
    public void fahrzieledit_setpublic(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit setlocation <destination>")
    @CommandDescription("Legt die Marker-Position (Dynmap) des aktuellen Fahrziel fest")
    @CommandPermission("craftbahn.command.destination.edit.setlocation")
    public void fahrzieledit_setlocation(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit settype <destination> <type>")
    @CommandDescription("Legt den Typ des angegebenen Fahrziel fest")
    @CommandPermission("craftbahn.command.destination.edit.settype")
    public void fahrzieledit_settype(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination,
            final @Argument(value="type", suggestions="destinationType") String type
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit setwarp <destination>")
    @CommandDescription("Legt die Warp-Position des aktuellen Fahrziel fest")
    @CommandPermission("craftbahn.command.destination.edit.setwarp")
    public void fahrzieledit_setwarp(
            final CommandSender sender,
            final @Argument(value="destination", suggestions="destinationName") String destination
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit updatemarker")
    @CommandDescription("Alle Dynmap-Marker werden neu geladen")
    @CommandPermission("craftbahn.command.destination.edit.updatemarker")
    public void fahrzieledit_updatemarker(
            final CommandSender sender
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit reload")
    @CommandDescription("Konfiguration wird neu geladen")
    @CommandPermission("craftbahn.command.destination.edit.reload")
    public void fahrzieledit_reload(
            final CommandSender sender
    ) {
        sender.sendMessage("Test");
    }

}
