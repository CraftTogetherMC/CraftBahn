package de.crafttogether.craftbahn;

import java.util.*;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.CTLocation;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.util.MarkerManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

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

            for (Destination dst : DestinationStorage.getDestinations()) {
                if (showAll) {
                    list.add(dst);
                } else if (dst.getType().equals(Destination.DestinationType.STATION))
                    list.add(dst);
            }

            if (list.size() > 0) {
                Destination dest = list.get(0);

                Random rand = new Random();
                dest = list.get(rand.nextInt(list.size()));

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
                TextComponent newLine = new TextComponent(ComponentSerializer.parse("{text: \"\n\"}"));
                sendMessage(p, "&e-------------- &c&lCraftBahn &e--------------");

                TextComponent message = new TextComponent();
                message.addExtra((BaseComponent)newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &6Willkommen bei der CraftBahn!")));
                message.addExtra((BaseComponent)newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &eUnser Schienennetz erstreckt sich")));
                message.addExtra((BaseComponent)newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &ein alle Himmelsrichtungen.")));
                message.addExtra((BaseComponent)newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8")));
                message.addExtra((BaseComponent)newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &6&lAnleitung: ")));
                p.spigot().sendMessage((BaseComponent)message);

                message = new TextComponent();
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &c/bahnhof")));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bahnhof"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&eHowTo: &6Bahnhof"))).create()));
                p.spigot().sendMessage((BaseComponent)message);

                message = new TextComponent();
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8")));
                message.addExtra((BaseComponent)newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &6&lFahrziel wählen")));
                p.spigot().sendMessage((BaseComponent)message);

                message = new TextComponent();
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &c/fahrziele")));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Alle Fahrziele auflisten"))).create()));
                p.spigot().sendMessage((BaseComponent)message);
                sendMessage(p, "&6CraftBahn &8» &eoder");

                message = new TextComponent();
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &c/fahrziel &7<name>")));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/fahrziel "));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Lege ein Fahrziel fest mit &c/fahrziel &7<name>"))).create()));
                p.spigot().sendMessage((BaseComponent)message);

                message = new TextComponent();
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8")));
                message.addExtra((BaseComponent) newLine);
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6CraftBahn &8» &eGute Fahrt!")));

                p.spigot().sendMessage((BaseComponent)message);
                message = new TextComponent(new BaseComponent[] { (BaseComponent)newLine });
                message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&e----------------------------------------")));
                message.addExtra((BaseComponent)newLine);
                p.spigot().sendMessage((BaseComponent)message);
                return true;
            }

            Destination dest = DestinationStorage.getDestination(args[0]);
            if (dest == null) {
                sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen.");
                return true;
            }

            if (!dest.isPublic().booleanValue() && !p.hasPermission("ctdestinations.see.private")) {
                sendMessage(p, "&6CraftBahn &8» &cAuf dieses Ziel hast du keinen Zugriff.");
                return true;
            }

            Bukkit.getServer().dispatchCommand((CommandSender)p, "train destination " + dest.getName());
            return true;
        }

        else if (cmd.getName().equalsIgnoreCase("fahrziele")) {
            if (sender instanceof Player)
                p = Bukkit.getPlayer(((Player)sender).getUniqueId());

            if (p == null)
                return false;

            List<Destination> destinations = DestinationStorage.getDestinations();
            if (destinations.size() < 1) {
                sendMessage(p, "&6CraftBahn &8» &cEs ist noch kein Ziel in der Liste. Tippe &e/destination add <name> &cum eines zu speichern.");
                return true;
            }

            sendMessage(p, "&e-------- &c&lCraftBahn &e| &6&lFahrziele &e--------");
            sendMessage(p, "");

            if (args.length < 1) {
                sendDestinationList(p, Destination.DestinationType.MAIN_STATION);
                sendDestinationList(p, Destination.DestinationType.STATION);
                sendDestinationList(p, Destination.DestinationType.PUBLIC_STATION);
                sendDestinationList(p, Destination.DestinationType.PLAYER_STATION);

                if (p.hasPermission("ctdestinations.see.private"))
                    sendDestinationList(p, null, true);
            }

            else {
                if (args[0].equalsIgnoreCase("privat") || args[0].equalsIgnoreCase("private")) {
                    sendDestinationList(p, null, true);
                    sendMessage(p, "");
                    sendMessage(p, "&e---------------------------------------");
                    return true;
                }

                String _type = args[0].replace("h", "hof");
                Destination.DestinationType type = Destination.findType(_type);

                if (type == null) {
                    try {
                        type = Destination.DestinationType.valueOf(_type);
                    } catch (Exception exception) {
                    }
                }

                if (type == null) {
                    sendMessage(p, "&6CraftBahn &8» &cDie Kategorie '" + _type + "' existiert nicht.");
                } else {
                    sendDestinationList(p, type);
                }
            }

            sendMessage(p, "");
            sendMessage(p, "&e----------------------------------------");
        }

        else if (cmd.getName().equalsIgnoreCase("fahrzieledit")) {
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

                if (DestinationStorage.getDestination(args[1]) != null) {
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

                Destination dest = DestinationStorage.getDestination(args[1]);
                if (dest == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen.");
                    return true;
                }

                sendMessage(p, "&6CraftBahn &8» &aZiel gelöscht.");
                MarkerManager.deleteMarker(dest);
                DestinationStorage.deleteDestination(dest.getName());
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

                if (DestinationStorage.getDestination(args[1]) == null) {
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

                Destination dest = DestinationStorage.getDestination(args[1]);
                dest.setOwner(owner.getUniqueId());

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, success) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else
                        sendMessage(finalP, "&6CraftBahn &8» &f'&e" + dest.getName() + "&f' &6gehört nun &e" + owner.getName());
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

                if (DestinationStorage.getDestination(args[1]) == null) {
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

                Destination dest = DestinationStorage.getDestination(args[1]);
                dest.setType(type);

                // Speichern
                Player finalP = p;
                Destination.DestinationType finalType = type;
                DestinationStorage.update(dest, (err, success) -> {
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

                if (DestinationStorage.getDestination(args[1]) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1]);
                dest.setLocation(CTLocation.fromBukkitLocation(p.getLocation()));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, success) -> {
                    if (err != null)
                        sendMessage(finalP, "&6CraftBahn &8» Es trat ein Fehler beim speichern der Änderungen auf. Bitte kontaktiere einen Administrator.");
                    else {
                        MarkerManager.setMarker(dest);
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

                if (DestinationStorage.getDestination(args[1]) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1]);
                dest.setPublic(Boolean.valueOf(false));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, success) -> {
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

                if (DestinationStorage.getDestination(args[1]) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1]);
                dest.setPublic(Boolean.valueOf(true));

                // Speichern
                Player finalP = p;
                DestinationStorage.update(dest, (err, success) -> {
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
                List<Destination> destinations = DestinationStorage.getDestinations();

                for (Destination dest : destinations)
                    MarkerManager.setMarker(dest, true);

                sendMessage(p, "&6CraftBahn &8» &6Es wurden &e" + destinations.size() + " &6Marker erneuert.");
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

                if (DestinationStorage.getDestination(args[1]) == null) {
                    sendMessage(p, "&6CraftBahn &8» &cEs existiert kein Ziel mit diesem Namen");
                    return true;
                }

                Destination dest = DestinationStorage.getDestination(args[1]);
                p.teleport(dest.getTeleportLocation().getBukkitLocation());

                sendMessage(p, "&6CraftBahn &8» &6Du wurdest zum Ziel: &f'&e" + dest.getName() + "&f' &6teleportiert.");
            }
        }
        return true;
    }

    private void sendDestinationList(Player p, Destination.DestinationType type) {
        sendDestinationList(p, type, false);
    }

    private void sendDestinationList(Player p, Destination.DestinationType type, boolean onlyPrivate) {
        String listName = "";
        sendMessage(p, "&6&l" + (onlyPrivate ? "Private Bahnhöfe:" : (type.equals(Destination.DestinationType.MAIN_STATION) ? type.toString() : type.toString().replace("hof", "höfe").toString().replace("Öffentlicher", "Öffentliche"))) + "&f:");

        for (Destination dest : DestinationStorage.getDestinations()) {
            if (onlyPrivate ? dest.isPublic().booleanValue() : (dest.getType() != type || (!dest.isPublic().booleanValue() && !p.hasPermission("ctdestinations.see.private"))))
                continue;

            String hoverText = "/fahrziel " + dest.getName();
            if ((dest.getType().equals(Destination.DestinationType.PLAYER_STATION) || dest.getType().equals(Destination.DestinationType.PUBLIC_STATION)) && dest.getOwner() != null && p.hasPermission("ctdestinations.see.owner")) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(dest.getOwner());
                if (owner.hasPlayedBefore())
                    hoverText += "\n&6Besitzer: &e" + owner.getName();
            }

            if (dest.getLocation() != null && p.hasPermission("ctdestinations.see.location"))
                hoverText += "\n&6Koordinaten: &e" + dest.getLocation().getX() + ", " + dest.getLocation().getY() + ", " + dest.getLocation().getZ();

            TextComponent message = new TextComponent();
            message.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7 > &e" + dest.getName())));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/fahrziel " + dest.getName()));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText))).create()));

            if (dest.getLocation() != null && p.hasPermission("ctdestinations.see.location")) {
                Location loc = dest.getTeleportLocation().getBukkitLocation();
                TextComponent tp = new TextComponent();
                tp.addExtra((BaseComponent)new TextComponent(ChatColor.translateAlternateColorCodes('&', " &7[&fTP&7]")));
                tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrzieledit tp " + dest.getName()));
                tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Teleportiere zum Zielort"))).create()));
                message.addExtra((BaseComponent)tp);
            }

            p.spigot().sendMessage((BaseComponent)message);
        }
    }

    private void sendMessage(Player p, String message) {
        if (p.isOnline())
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
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

        else if (cmd.getName().equalsIgnoreCase("fahrziele")) {
            for (Destination.DestinationType type : Destination.DestinationType.values()) {
                if (!type.equals(Destination.DestinationType.MAIN_STATION))
                    proposals.add(type.toString().replace("hof", "höfe"));
            }
        }

        else if (cmd.getName().equalsIgnoreCase("fahrzieledit")) {
            if (args.length == 1) {
                if (sender.hasPermission("ctdestinations.edit.add"))
                    proposals.add("add");
                if (sender.hasPermission("ctdestinations.edit.remove"))
                    proposals.add("remove");
                if (sender.hasPermission("ctdestinations.edit.setowner"))
                    proposals.add("setowner");
                if (sender.hasPermission("ctdestinations.edit.setlocation"))
                    proposals.add("setlocation");
                if (sender.hasPermission("ctdestinations.edit.settype"))
                    proposals.add("settype");
                if (sender.hasPermission("ctdestinations.edit.setpublic"))
                    proposals.add("setpublic");
                if (sender.hasPermission("ctdestinations.edit.setprivate"))
                    proposals.add("setprivate");
                if (sender.hasPermission("ctdestinations.edit.updatemarker"))
                    proposals.add("updatemarker");
                if (sender.hasPermission("ctdestinations.teleport"))
                    proposals.add("tp");
            }

            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add") && sender.hasPermission("ctdestinations.edit.add")) {
                    proposals.add("<name>");
                } else {
                    if (!sender.hasPermission("ctdestinations.edit." + args[0]))
                        return new ArrayList<>();

                    List<Destination> destinations = DestinationStorage.getDestinations();
                    for (Destination dest : DestinationStorage.getDestinations()) {
                        if ((args[0].equalsIgnoreCase("setprivate") && !dest.isPublic().booleanValue()) || (
                                args[0].equalsIgnoreCase("setpublic") && dest.isPublic().booleanValue()))
                            continue;

                        proposals.add(dest.getName());
                    }
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("setowner") && sender.hasPermission("ctdestinations.edit.setowner")) {
                    for (Player p : Bukkit.getOnlinePlayers())
                        proposals.add(p.getName());
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
