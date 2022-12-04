package de.crafttogether.craftbahn.destinations;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class DestinationList {
    private final List<Destination> destinations;
    private final List<Component> pages;

    private Destination.DestinationType filterType;
    private String filterName;

    private int rowsPerPage = 8;
    private boolean showOwner = false;
    private boolean showLocation = false;
    private boolean showContents = false;
    private boolean showFooter = false;
    private boolean showType = true;

    public DestinationList(List<Destination> destinations) {
        this.destinations = destinations;
        this.pages = new ArrayList<>();
        this.filterType = null;
    }

    public Component getContentsPage() {
        MiniMessage parser = CraftBahnPlugin.plugin.getMiniMessageParser();

        // Components
        Component head = Message.deserialize("&e-------------- &c&lCraftBahn &e--------------")
            .append(Component.newline())
            .append(Component.newline())
            .append(parser.deserialize("<prefix><yellow>Guten Tag, Reisender!</yellow>"))
            .append(Component.newline())
            .append(parser.deserialize("<prefix><yellow>Verstehst du nur </yellow>"))
            .append(parser.deserialize("<red>/bahnhof</red><yellow>?</yellow>"))
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/bahnhof"))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, parser.deserialize("<dark_green>Informationen zum Schienennetz<dark_green>")));

        Component btnMainStations = Message.deserialize("&6CraftBahn &8» &2> &eHauptbahnhöfe")
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele MAIN_STATION 2"))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&2Hauptbahnhöfe")));

        Component btnStations = Message.deserialize("&6CraftBahn &8» &2> &eSystem-Bahnhöfe")
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele STATION 2"))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&2Bahnhöfe")));

        Component btnPublicStations = Message.deserialize("&6CraftBahn &8» &2> &eÖffentliche Bahnhöfe")
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele PUBLIC_STATION 2"))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&2Öffentlich")));

        Component btnPlayerStations = Message.deserialize("&6CraftBahn &8» &2> &eSpieler-Bahnhöfe")
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele PLAYER_STATION 2"))
            .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&2Spielerbahnhöfe")));

        // Build page
        return Component.text("")
            .append(head)
            .append(Component.newline())

            .append(Message.deserialize("&6CraftBahn &8»"))
            .append(Component.newline())
            .append(Message.deserialize("&6CraftBahn &8» &6&lMögliche Fahrziele:"))
            .append(Component.newline())
            .append(Message.deserialize("&6CraftBahn &8»"))
            .append(Component.newline())

            .append(btnMainStations)
            .append(Component.newline())
            .append(btnStations)
            .append(Component.newline())
            .append(btnPublicStations)
            .append(Component.newline())
            .append(btnPlayerStations)
            .append(Component.newline());
    }

    public void build() {
        Component page = Component.text("");
        int row = 0;

        // Add contents-page
        if (this.showContents)
            this.pages.add(this.getContentsPage());

        TreeMap<String, List<Destination>> serverMap = new TreeMap<>();

        for (Destination dest : this.destinations) {
            if (this.filterType != null && !dest.getType().equals(this.filterType))
                continue;

            if (!serverMap.containsKey(dest.getServer()))
                serverMap.put(dest.getServer(), new ArrayList<>());

            serverMap.get(dest.getServer()).add(dest);
        }

        List<String> keys = new ArrayList<>(serverMap.keySet());
        List<String> sortedList = new ArrayList<>();

        String firstKey = null;
        String lastKey = null;

        for (String key : keys) {
            Message.debug(key + " - " + CraftBahnPlugin.plugin.getServerName());
            if (key.equalsIgnoreCase(CraftBahnPlugin.plugin.getServerName()))
                firstKey = key;
            else if (key.equalsIgnoreCase("creative"))
                lastKey = key;
            else
                sortedList.add(key);
        }

        if (firstKey != null)
            sortedList.add(0, firstKey);

        if (lastKey != null)
            sortedList.add(sortedList.size(), lastKey);

        for (String serverName : sortedList) {
            if ((this.rowsPerPage - row) < 4) {
                // New Page
                this.pages.add(page);
                page = Component.text("");
                row = 0;
            }

            if (row != 0) {
                page = page.append(Component.newline());
                row++;
            }

            page = page
                .append(Message.deserialize("&6CraftBahn &8» &7# &6&l" + capitalize(serverName) + ":"))
                .append(Component.newline());

            row = row + 2;

            int items = 0;
            for (Destination dest : serverMap.get(serverName)) {
                row++;

                Component btnFahrziel;
                if (dest.getType() == Destination.DestinationType.PLAYER_STATION)
                    btnFahrziel = Message.deserialize("&6CraftBahn &8» &e" + dest.getName());
                else
                    btnFahrziel = Message.deserialize("&6CraftBahn &8» &6" + dest.getName());

                // Build hoverText
                Collection<Destination> duplicates = CraftBahnPlugin.plugin.getDestinationStorage().getDestinations(dest.getName());

                String hoverText = "&2/fahrziel " + dest.getName() + (duplicates.size() > 1 ? (" &7" + dest.getServer()) : "");
                if (this.showType) hoverText += "\n&6Stations-Typ: &e" + dest.getType().toString();

                if ((dest.getType().equals(Destination.DestinationType.PLAYER_STATION) || dest.getType().equals(Destination.DestinationType.PUBLIC_STATION)) && dest.getOwner() != null && this.showOwner) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(dest.getOwner());

                    StringBuilder strOwner = new StringBuilder((owner.hasPlayedBefore() ? owner.getName() : "Unbekannt") + ", ");
                    for (UUID uuid : dest.getParticipants()) {
                        OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                        if (!participant.hasPlayedBefore()) continue;
                        strOwner.append(participant.getName()).append(", ");
                    }

                    hoverText += "\n&6Besitzer: &e" + strOwner.substring(0, strOwner.length()-2);
                }

                if (dest.getLocation() != null && this.showLocation) {
                    hoverText += "\n&6Koordinaten: &e" + Math.round(dest.getLocation().getX()) + ", " + Math.round(dest.getLocation().getY()) + ", " + Math.round(dest.getLocation().getZ());
                    hoverText += "\n&6Welt: &e" + dest.getWorld();
                }

                btnFahrziel = btnFahrziel
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziel " + dest.getName() + (duplicates.size() > 1 ? (" --server " + dest.getServer()) : "")))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize(hoverText)));

                // Show teleport-button
                if (dest.getLocation() != null && this.showLocation) {
                    Component btnTeleport = Message.deserialize(" &7[&fTP&7]")
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrzieledit tp " + dest.getName()))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&6Teleportiere zum Zielort")));
                    btnFahrziel = btnFahrziel.append(btnTeleport);
                }

                page = page
                    .append(btnFahrziel)
                    .append(Component.newline());

                items++;

                // New Page
                if (row >= this.rowsPerPage && serverMap.get(serverName).size() > items) {
                    this.pages.add(page);
                    page = Component.text("");
                    row = 0;
                }
            }
        }

        this.pages.add(page);
    }

    public Component renderPage(int pageIndex) {
        if (this.pages.size() < 1)
            return null;

        Component page = this.pages.get(pageIndex - 1);
        Component output = Component.newline().append(page);

        String filter = (filterType == null ? "" : filterType.name() + " ");
        filter = (filterName == null ? filter : filterName + " ");
        String command = (filterName != null) ? "/fahrziel" : "/fahrziele";

        if (pages.size() > 1) {
            output = output.append(Component.newline());

            Component btnPrevious;
            if (pageIndex > 1) {
                btnPrevious = Message.deserialize("&a----<< &6Zurück")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + filter + "--page " + (pageIndex - 1)))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&6Vorherige Seite: &e" + (pageIndex - 1))));
            } else
                btnPrevious = Message.deserialize("&2----<< &7Zurück");

            Component btnForward;
            if (pageIndex < this.pages.size()) {
                btnForward = Message.deserialize("&6Weiter &2>>----")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + filter + "--page " + (pageIndex + 1)))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Message.deserialize("&6Nächste Seite: &e" + (pageIndex + 1))));
            } else
                btnForward = Message.deserialize("&7Weiter >>----");

            output = output
                .append(btnPrevious)
                .append(Message.deserialize(" &2" + pageIndex + "&7/&2" + pages.size() + " "))
                .append(btnForward)
                .append(Component.newline());
        }

        return output;
    }

    public void setPage(int i, Component page) {
        this.pages.set(i, page);
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public void setFilterType(Destination.DestinationType destinationType) {
        this.filterType = destinationType;
    }

    public void setFilterName(String destinationName) {
        this.filterName = destinationName;
    }

    public Component getPage(int i) {
        return this.pages.get(i);
    }

    public int getPageCount() {
        return pages.size();
    }

    public void sendPage(Player player, int pageIndex) {
        // Build pages
        build();

        if (pageIndex < 1) {
            player.sendMessage(Message.deserialize("&6CraftBahn &8» &cUngültige Seitennummer."));
        }
        else if (pageIndex > getPageCount()) {
            player.sendMessage(Message.deserialize("&6CraftBahn &8» &cEs gibt nur " + getPageCount() + " Seite" + (getPageCount() > 1 ? "n":"")));
            return;
        }
        else {
            Component message = renderPage(pageIndex);

            if (this.showFooter) {
                message = message
                    .append(Component.newline())
                    .append(Message.deserialize("&e----------------------------------------"));
            }

            player.sendMessage(message);
        }
    }

    private String capitalize(String name) {
        String firstLetter = name.substring(0, 1);
        String remainingLetters = name.substring(1);
        firstLetter = firstLetter.toUpperCase();
        return firstLetter + remainingLetters;
    }

    public void showOwner(boolean show) {
        this.showOwner = show;
    }
    public void showLocation(boolean show) {
        this.showLocation = show;
    }
    public void showContents(boolean show) {
        this.showContents = show;
    }
    public void showFooter(boolean show) {
        this.showFooter = show;
    }
    public void showType(boolean show) {
        this.showType = show;
    }
}
