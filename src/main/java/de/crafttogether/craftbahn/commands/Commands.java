package de.crafttogether.craftbahn.commands;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationList;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.MarkerManager;
import de.crafttogether.craftbahn.util.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class Commands implements TabExecutor {
    private CraftBahn plugin = CraftBahn.getInstance();

    public boolean onCommand(CommandSender sender, Command cmd, String st, String[] args) {
        Player p = null;

        if (cmd.getName().equalsIgnoreCase("rbf") || cmd.getName().equalsIgnoreCase("rt") || cmd.getName().equalsIgnoreCase("rtp")) {
            List<Destination> list = new ArrayList<Destination>();

            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            boolean showAll = false;
            if (args.length > 0 && args[0].equalsIgnoreCase("all"))
                showAll = true;

            List<Destination> destinations = new ArrayList<>(DestinationStorage.getDestinations());
            destinations = DestinationStorage.filterByServer(destinations, CraftBahn.getInstance().getServerName());

            for (Destination dst : destinations) {
                if (showAll) {
                    list.add(dst);
                } else if (dst.getType().equals(Destination.DestinationType.STATION))
                    list.add(dst);
            }

            if (list.size() > 0) {
                Random rand = new Random();
                Destination dest = list.get(rand.nextInt(list.size()));

                sendMessage(p, "&6CraftBahn &8» &cDu wurdest zum " + dest.getType() + " &f'&e" + dest.getName() + "'&f &cteleportiert.");
                p.teleport(dest.getTeleportLocation().getBukkitLocation());
            }
            else
                sendMessage(p, "&6CraftBahn &8» &cEs wurde kein mögliches Ziel gefunden.");
        }

        else if (cmd.getName().equalsIgnoreCase("fahrziel")) {
            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            if (args.length == 0) {
                sendMessage(p, "&e-------------- &c&lCraftBahn &e--------------");

                TextComponent message = new TextComponent();
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8» &6Willkommen bei der CraftBahn!"));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8» &eUnser Schienennetz erstreckt sich"));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8» &ein alle Himmelsrichtungen."));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8"));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8» &6&lAnleitung: "));
                p.spigot().sendMessage(message);

                message = new TextComponent();
                message.addExtra(Message.format("&6CraftBahn &8» &c/bahnhof"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bahnhof"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&eHowTo: &6Bahnhof"))).create()));
                p.spigot().sendMessage(message);

                message = new TextComponent();
                message.addExtra(Message.format("&6CraftBahn &8"));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8» &6&lFahrziel wählen"));
                p.spigot().sendMessage(message);

                message = new TextComponent();
                message.addExtra(Message.format("&6CraftBahn &8» &c/fahrziele"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&6Alle Fahrziele auflisten"))).create()));
                p.spigot().sendMessage(message);
                sendMessage(p, "&6CraftBahn &8» &eoder");

                message = new TextComponent();
                message.addExtra(Message.format("&6CraftBahn &8» &c/fahrziel &7<name>"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/fahrziel "));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&6Lege ein Fahrziel fest mit &c/fahrziel &7<name>"))).create()));
                p.spigot().sendMessage(message);

                message = new TextComponent();
                message.addExtra(Message.format("&6CraftBahn &8"));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&6CraftBahn &8» &eGute Fahrt!"));
                message.addExtra(Message.newLine());
                message.addExtra(Message.format("&e----------------------------------------"));
                message.addExtra(Message.newLine());
                p.spigot().sendMessage(message);
                return true;
            }

            String serverName = null;
            List<Destination> found = new ArrayList<>();
            if (args.length == 2) serverName = args[1];

            if (serverName != null)
                found.add(DestinationStorage.getDestination(args[0], serverName));
            else
                found = new ArrayList<>(DestinationStorage.getDestinations(args[0]));

            if (found.size() < 1) {
                sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel dem Namen &f'&e" + args[0] + "&f'" + (serverName != null ? " &cauf dem angegebenen Server." : ""));
                return true;
            }

            else if (found.size() > 1) {
                DestinationList list = new DestinationList(found);
                list.showOwner(true);
                list.showLocation(true);
                p.sendMessage(Message.format("&6CraftBahn &8» &cEs wurden mehrere mögliche Ziele gefunden:"));
                list.sendPage(p, 1);
                return true;
            }

            Destination dest = found.get(0);

            if (!dest.isPublic().booleanValue() && !p.hasPermission("ctdestinations.see.private")) {
                sendMessage(p, "&6CraftBahn &8» &cAuf dieses Ziel hast du keinen Zugriff.");
                return true;
            }

            if (!CraftBahn.getInstance().getServerName().equalsIgnoreCase(dest.getServer())) {
                Bukkit.getServer().dispatchCommand(p, "train route set " + dest.getServer() + " " + dest.getName());
                Bukkit.getServer().dispatchCommand(p, "train destination " + dest.getServer());
                sendMessage(p, "&6CraftBahn &8» &3Set route: " + dest.getServer() + " -> " + dest.getName());
            } else {
                sendMessage(p, "&6CraftBahn &8» Set destination: " + dest.getName());
                Bukkit.getServer().dispatchCommand(p, "train destination " + dest.getName());
            }

            return true;
        }

        else if (cmd.getName().equalsIgnoreCase("fahrzieledit") || cmd.getName().equalsIgnoreCase("fze")) {
            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            if (args.length == 0) {
                sendMessage(p, "&e/fahrziel &7<name> &f- &6Fahrziel festlegen");
                sendMessage(p, "&e/fahrziele &7[kategorie] &f- &6Fahrziele auflisten");
                sendMessage(p, "&e/fahrzieledit add &7<> &f- &6Neues Fahrziel anlegen");
                sendMessage(p, "&e/fahrzieledit remove &7<name> &f- &6Fahrziel entfernen");
                sendMessage(p, "&e/fahrzieledit settype &7<name> <typ> &f- &6Kategorie aktualisieren");
                sendMessage(p, "&e/fahrzieledit setowner &7<name> &f- &6Besitzer aktualisieren");
                sendMessage(p, "&e/fahrzieledit setlocation &7<name> &f- &6Position / Marker aktualisieren");
                sendMessage(p, "&e/fahrzieledit setprivate &7<name> &f- &6Fahrziel verstecken");
                sendMessage(p, "&e/fahrzieledit setpublic &7<name> &f- &6Fahrziel wieder anzeigen");
                sendMessage(p, "&e/fahrzieledit tp &7<name> &f- &6Teleportiere zu Fahrziel");
                sendMessage(p, "&e/fahrzieledit updatemarker &7<name> &f- &6Update alle Dynmap-Marker");
            }

            else if (args[0].equalsIgnoreCase("add")) {
                boolean isPublic = true;

                if (!p.hasPermission("ctdestinations.edit.add")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) != null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert bereits ein Ziel mit diesem Namen in der Liste.");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Stationstyp angegeben");
                    return true;
                }

                if (args.length == 4 && (args[3].equalsIgnoreCase("private") || args[3].equalsIgnoreCase("hidden")))
                    isPublic = false;

                Destination.DestinationType type = null;

                try {
                    type = Destination.DestinationType.valueOf(args[2].toUpperCase());
                } catch (Exception exception) {}

                if (type == null) {
                    sendMessage(p, "&6CraftBahn &8» &cUngültiger Stationstyp.");
                    return true;
                }

                Player finalP = p;
                DestinationStorage.addDestination(args[1], p.getUniqueId(), type, p.getLocation(), Boolean.valueOf(isPublic), (err, id) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern des Fahrziel auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &aFahrziel wurde erstellt. ID: " + id);
                });
            }

            else if (args[0].equalsIgnoreCase("remove")) {
                if (!p.hasPermission("ctdestinations.edit.remove")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2 || args[1].equals("") || args[1].length() < 1) {
                    sendMessage(p, "&6CraftBahn &8» &cBitte gebe den Namen des Ziel ein.");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                if (dest == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen.");
                    return true;
                }

                Player finalP = p;
                DestinationStorage.delete(dest.getId(), (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        MarkerManager.deleteMarker(dest);
                        sendMessage(finalP, "&6CraftBahn &8» &aZiel gelöscht.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("setowner")) {
                if (!p.hasPermission("ctdestinations.edit.setowner")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &6CraftBahn &8&cEs wurde kein Ziel angegeben.");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Spieler angegeben");
                    return true;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
                if (owner == null || !owner.hasPlayedBefore()) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Spieler mit dem Namen &e" + args[2] + " &cgefunden");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.setOwner(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &f'&e" + dest.getName() + "&f' &6gehört nun &e" + owner.getName());
                });
            }

            else if (args[0].equalsIgnoreCase("addmember")) {
                if (!p.hasPermission("ctdestinations.edit.addmember")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &6CraftBahn &8&cEs wurde kein Ziel angegeben.");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Spieler angegeben");
                    return true;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
                if (owner == null || !owner.hasPlayedBefore()) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Spieler mit dem Namen &e" + args[2] + " &cgefunden");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.addParticipant(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &6Du hast &e" + owner.getName() + " &6als Besitzer des Fahrziel &f'&e" + dest.getName() + " &2hinzugefügt.");
                });
            }

            else if (args[0].equalsIgnoreCase("removemember")) {
                if (!p.hasPermission("ctdestinations.edit.removemember")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &6CraftBahn &8&cEs wurde kein Ziel angegeben.");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                if (args.length < 3) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Spieler angegeben");
                    return true;
                }

                OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
                if (owner == null || !owner.hasPlayedBefore()) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Spieler mit dem Namen &e" + args[2] + " &cgefunden");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.removeParticipant(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &6Du hast &e" + owner.getName() + " &6als Besitzer des Fahrziel &f'&e" + dest.getName() + " &centfernt.");
                });
            }

            else if (args[0].equalsIgnoreCase("settype")) {
                if (!p.hasPermission("ctdestinations.edit.settype")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }
                if (args.length < 3) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Stationstyp angegeben");
                    return true;
                }

                Destination.DestinationType type = null;
                try {
                    type = Destination.DestinationType.valueOf(args[2].toUpperCase());
                } catch (Exception exception) {}

                if (type == null) {
                    sendMessage(p, "&6CraftBahn &8» &cUngültiger Stationstyp.");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.setType(type);

                // Speichern
                Player finalP = p;
                Destination.DestinationType finalType = type;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &e&f'" + dest.getName() + "&f' &6ist nun ein &e" + finalType);
                });
            }

            else if (args[0].equalsIgnoreCase("setlocation")) {
                if (!p.hasPermission("ctdestinations.edit.setlocation")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.setLocation(CTLocation.fromBukkitLocation(p.getLocation()));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        MarkerManager.setMarker(dest);
                        sendMessage(finalP, "&6CraftBahn &8» &6Du hast die Position für das Ziel: &f'&e" + dest.getName() + "&f' &6aktualisiert.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("setwarp")) {
                if (!p.hasPermission("ctdestinations.edit.setwarp")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.setTeleportLocation(CTLocation.fromBukkitLocation(p.getLocation()));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        sendMessage(finalP, "&6CraftBahn &8» &6Du hast die Position für das Ziel: &f'&e" + dest.getName() + "&f' &6aktualisiert.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("setprivate")) {
                if (!p.hasPermission("ctdestinations.edit.setprivate")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.setPublic(Boolean.valueOf(false));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        sendMessage(finalP, "&6CraftBahn &8» &6Das Ziel: &f'&e" + dest.getName() + "'&f &6ist nun &cprivat");
                    }
                });
            }
            else if (args[0].equalsIgnoreCase("setpublic")) {
                if (!p.hasPermission("ctdestinations.edit.setpublic")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                dest.setPublic(Boolean.valueOf(true));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, affectedRows) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        sendMessage(finalP, "&6CraftBahn &8» &6Das Ziel: &f'&e" + dest.getName() + "&f' &6ist nun &2öffentlich.");
                    }
                });
            }

            else if (args[0].equalsIgnoreCase("updatemarker")) {
                if (!p.hasPermission("ctdestinations.edit.updatemarker")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                MarkerManager.createMarkerSets();
                Collection<Destination> destinations = DestinationStorage.getDestinations();

                for (Destination dest : destinations)
                    MarkerManager.setMarker(dest, true);

                sendMessage(p, "&6CraftBahn &8» &6Es wurden &e" + destinations.size() + " &6Marker erneuert.");
            }

            else if (args[0].equalsIgnoreCase("reload")) {
                if (!p.hasPermission("ctdestinations.edit.reload")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                Player finalP = p;
                DestinationStorage.loadAll((err, destinations) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» &cBeim laden der Fahrziele ist ein Fehler aufgetreten");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &2Es wurden &a" + destinations.size() + " &2Fahrziele geladen.");
                });
            }

            else if (args[0].equalsIgnoreCase("tp")) {
                if (!p.hasPermission("ctdestinations.teleport")) {
                    sendMessage(p, "&cDazu hast du keine Berechtigung.");
                    return true;
                }

                if (args.length < 2) {
                    sendMessage(p, "&6CraftBahn &8» &cEs wurde kein Ziel angegeben");
                    return true;
                }

                if (DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName()) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());

                if (dest.getTeleportLocation().getServer().equalsIgnoreCase(CraftBahn.getInstance().getServerName())) {
                    Location loc = dest.getTeleportLocation().getBukkitLocation();
                    p.teleport(loc);
                    sendMessage(p, "&6CraftBahn &8» &6Du wurdest zum Ziel: &f'&e" + dest.getName() + "&f' &6teleportiert.");
                }
                else
                    sendMessage(p, "&6CraftBahn &8» &cDas Ziel befindet sich auf einem anderen Server.");
            }
        }
        return true;
    }

    private void sendMessage(Player p, String message) {
        if (p.isOnline())
            p.sendMessage(Message.format(message));
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<String> proposals = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("fahrziel")) {
            for (Destination dest : DestinationStorage.getDestinations()) {
                if (!sender.hasPermission("ctdestinations.see.private") && !dest.isPublic().booleanValue())
                    continue;

                proposals.add(dest.getName());
            }
        }

        else if (cmd.getName().equalsIgnoreCase("fahrzieledit") || cmd.getName().equalsIgnoreCase("fze")) {
            if (args.length == 1) {
                if (sender.hasPermission("ctdestinations.edit.add"))
                    proposals.add("add");
                if (sender.hasPermission("ctdestinations.edit.remove"))
                    proposals.add("remove");
                if (sender.hasPermission("ctdestinations.edit.setowner"))
                    proposals.add("setowner");
                if (sender.hasPermission("ctdestinations.edit.addmember"))
                    proposals.add("addmember");
                if (sender.hasPermission("ctdestinations.edit.removemember"))
                    proposals.add("removemember");
                if (sender.hasPermission("ctdestinations.edit.setlocation"))
                    proposals.add("setlocation");
                if (sender.hasPermission("ctdestinations.edit.setwarp"))
                    proposals.add("setwarp");
                if (sender.hasPermission("ctdestinations.edit.settype"))
                    proposals.add("settype");
                if (sender.hasPermission("ctdestinations.edit.setpublic"))
                    proposals.add("setpublic");
                if (sender.hasPermission("ctdestinations.edit.setprivate"))
                    proposals.add("setprivate");
                if (sender.hasPermission("ctdestinations.edit.updatemarker"))
                    proposals.add("updatemarker");
                if (sender.hasPermission("ctdestinations.edit.reload"))
                    proposals.add("reload");
                if (sender.hasPermission("ctdestinations.teleport"))
                    proposals.add("tp");
            }

            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add") && sender.hasPermission("ctdestinations.edit.add")) {
                    proposals.add("<name>");
                } else {
                    if (!sender.hasPermission("ctdestinations.edit." + args[0]))
                        return new ArrayList<>();

                    Collection<Destination> destinations = DestinationStorage.getDestinations();
                    for (Destination dest : DestinationStorage.getDestinations()) {
                        if ((args[0].equalsIgnoreCase("setprivate") && !dest.isPublic().booleanValue()) || (
                                args[0].equalsIgnoreCase("setpublic") && dest.isPublic().booleanValue()))
                            continue;

                        proposals.add(dest.getName());
                    }
                }
            } else if (args.length == 3) {
                if ((args[0].equalsIgnoreCase("setowner") && sender.hasPermission("ctdestinations.edit.setowner")) ||
                        args[0].equalsIgnoreCase("addmember") && sender.hasPermission("ctdestinations.edit.addmember")) {
                    for (Player p : Bukkit.getOnlinePlayers())
                        proposals.add(p.getName());

                } else if (args[0].equalsIgnoreCase("removemember") && sender.hasPermission("ctdestinations.edit.removemember")) {
                    Destination dest = DestinationStorage.getDestination(args[1], CraftBahn.getInstance().getServerName());
                    if (dest != null) {
                        for (UUID uuid : dest.getParticipants()) {
                            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                            if (!participant.hasPlayedBefore()) continue;
                            proposals.add(participant.getName());
                        }
                    }

                } else if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("settype")) && (
                        sender.hasPermission("ctdestinations.edit.add") || sender.hasPermission("ctdestinations.edit.settype"))) {
                    proposals.add("STATION");
                    proposals.add("MAIN_STATION");
                    proposals.add("PUBLIC_STATION");
                    proposals.add("PLAYER_STATION");
                }
            } else if (args.length == 4 && args[0].equalsIgnoreCase("add") &&
                    sender.hasPermission("ctdestinations.edit.add")) {
                proposals.add("PRIVATE");
            }
        }

        if (args.length < 1 || args[args.length - 1].equals("")) {
            newList = proposals;
        } else {
            for (String value : proposals) {
                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    newList.add(value);
            }
        }

        return newList;
    }
}
