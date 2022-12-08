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

        // Filter: destinationType
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
    @CommandPermission("craftbahn.command.destedit.info")
    public void fahrzieledit_info(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server,
            final @Flag(value="page") Integer page
    ) {
        Destination destination = findDestination(sender, name, server);

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

    @CommandMethod(value="fahrzieledit tp <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Teleportiert den Spieler zum angegebenen Fahrziel")
    @CommandPermission("craftbahn.command.destedit.teleport")
    public void fahrzieledit_teleport(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);

        if (!destination.getServer().equalsIgnoreCase(plugin.getServerName())) {
            Localization.COMMAND_DESTEDIT_TELEPORT_OTHERSERVER.message(sender,
                    PlaceholderResolver.resolver("server", destination.getServer()));
            return;
        }

        // Teleport player
        sender.teleport(destination.getTeleportLocation().getBukkitLocation());

        Localization.COMMAND_DESTEDIT_TELEPORT.message(sender,
                PlaceholderResolver.resolver("destination", destination.getName()));
    }

    @CommandMethod(value="fahrzieledit add <destination> <type>", requiredSender=Player.class)
    @CommandDescription("Fügt ein neues Fahrziel der Liste hinzu")
    @CommandPermission("craftbahn.command.destedit.add")
    public void fahrzieledit_add(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="type", suggestions="destinationType") String type
    ) {
        Destination.DestinationType destinationType = Destination.findType(type);
        if (destinationType == null) {
            Localization.COMMAND_DESTEDIT_ADD_INVALIDTYPE.message(sender);
            return;
        }

        plugin.getDestinationStorage().addDestination(name, sender.getUniqueId(), destinationType, sender.getLocation(), true, (err, dest) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender,
                        PlaceholderResolver.resolver("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_ADD_SUCCESS.message(sender,
                        PlaceholderResolver.resolver("destination", dest.getName()));
        });
    }

    @CommandMethod(value="fahrzieledit remove <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Entfernt das angegebene Fahrziel aus der Liste")
    @CommandPermission("craftbahn.command.destedit.remove")
    public void fahrzieledit_remove(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);

        plugin.getDestinationStorage().delete(destination.getId(), (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender,
                        PlaceholderResolver.resolver("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_REMOVE.message(sender,
                        PlaceholderResolver.resolver("destination", destination.getName()));
        });
    }

    @CommandMethod(value="fahrzieledit addmember <destination> <player> [server]", requiredSender=Player.class)
    @CommandDescription("Fügt dem angegebene Fahrziel einen sekundären Besitzer hinzu")
    @CommandPermission("craftbahn.command.destedit.addmember")
    public void fahrzieledit_addmember(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="player", suggestions="onlinePlayers") String player,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        OfflinePlayer participant = Bukkit.getOfflinePlayer(player);
        if (!participant.hasPlayedBefore()) {
            Localization.COMMAND_DESTEDIT_UNKOWNPLAYER.message(sender,
                    PlaceholderResolver.resolver("input", player));
            return;
        }

        Destination destination = findDestination(sender, name, server);

        if (destination.getParticipants().contains(participant.getUniqueId())) {
            Localization.COMMAND_DESTEDIT_ADDMEMBER_FAILED.message(sender,
                    PlaceholderResolver.resolver("input", player));
            return;
        }

        destination.addParticipant(participant.getUniqueId());

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender,
                        PlaceholderResolver.resolver("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_ADDMEMBER_SUCCESS.message(sender,
                        PlaceholderResolver.resolver("destination", destination.getName()),
                        PlaceholderResolver.resolver("player", participant.getName()));
        });
    }

    @CommandMethod(value="fahrzieledit removemember <destination> <player> [server]", requiredSender=Player.class)
    @CommandDescription("Entfernt dem angegebene Fahrziel einen sekundären Besitzer")
    @CommandPermission("craftbahn.command.destedit.removemember")
    public void fahrzieledit_removemember(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="player", suggestions="onlinePlayers") String player,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        OfflinePlayer participant = Bukkit.getOfflinePlayer(player);
        if (!participant.hasPlayedBefore()) {
            Localization.COMMAND_DESTEDIT_UNKOWNPLAYER.message(sender,
                    PlaceholderResolver.resolver("input", player));
            return;
        }

        Destination destination = findDestination(sender, name, server);

        if (!destination.getParticipants().contains(participant.getUniqueId())) {
            Localization.COMMAND_DESTEDIT_REMOVEMEMBER_FAILED.message(sender,
                    PlaceholderResolver.resolver("input", player));
            return;
        }

        destination.removeParticipant(participant.getUniqueId());

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender,
                        PlaceholderResolver.resolver("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_REMOVEMEMBER_SUCCESS.message(sender,
                        PlaceholderResolver.resolver("destination", destination.getName()),
                        PlaceholderResolver.resolver("player", participant.getName()));
        });
    }

    @CommandMethod(value="fahrzieledit setowner <destination> <player> [server]", requiredSender=Player.class)
    @CommandDescription("Legt den primären Besitzer des angegebenen Fahrziel fest")
    @CommandPermission("craftbahn.command.destedit.setowner")
    public void fahrzieledit_setowner(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="player", suggestions="onlinePlayers") String player,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
    }

    @CommandMethod(value="fahrzieledit setprivate <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Macht das angegebene Fahrziel privat")
    @CommandPermission("craftbahn.command.destedit.setprivate")
    public void fahrzieledit_setprivate(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
    }

    @CommandMethod(value="fahrzieledit setpublic <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Macht das angegebene Fahrziel öffentlich")
    @CommandPermission("craftbahn.command.destedit.setpublic")
    public void fahrzieledit_setpublic(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
    }

    @CommandMethod(value="fahrzieledit setlocation <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Legt die Marker-Position (Dynmap) des aktuellen Fahrziel fest")
    @CommandPermission("craftbahn.command.destedit.setlocation")
    public void fahrzieledit_setlocation(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
    }

    @CommandMethod(value="fahrzieledit settype <destination> <type> [server]", requiredSender=Player.class)
    @CommandDescription("Legt den Typ des angegebenen Fahrziel fest")
    @CommandPermission("craftbahn.command.destedit.settype")
    public void fahrzieledit_settype(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="type", suggestions="destinationType") String type,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
    }

    @CommandMethod(value="fahrzieledit setwarp <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Legt die Warp-Position des aktuellen Fahrziel fest")
    @CommandPermission("craftbahn.command.destedit.setwarp")
    public void fahrzieledit_setwarp(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
    }

    @CommandMethod(value="fahrzieledit updatemarker", requiredSender=Player.class)
    @CommandDescription("Alle Dynmap-Marker werden neu geladen")
    @CommandPermission("craftbahn.command.destedit.updatemarker")
    public void fahrzieledit_updatemarker(
            final Player sender
    ) {
        sender.sendMessage("Test");
    }

    @CommandMethod(value="fahrzieledit reload", requiredSender=Player.class)
    @CommandDescription("Konfiguration wird neu geladen")
    @CommandPermission("craftbahn.command.destedit.reload")
    public void fahrzieledit_reload(
            final Player sender
    ) {
        sender.sendMessage("Test");
    }

    public Destination findDestination(CommandSender sender, String name, String server) {
        if (LogicUtil.nullOrEmpty(name)) {
            Localization.COMMAND_DESTEDIT_NONAME.message(sender);
            return null;
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
        else if (result.size() > 1)
            Localization.COMMAND_DESTEDIT_MULTIPLEDEST.message(sender);

        // A single destination was found
        else
            return result.get(0);

        return null;
    }
}
