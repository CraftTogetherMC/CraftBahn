package de.crafttogether.craftbahn.destinations;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class DestinationList {
    private CraftBahnPlugin plugin;

    private final List<Destination> destinations;
    private final List<Component> pages;

    private int rowsPerPage = 8;
    private boolean showOwner = false;
    private boolean showLocation = false;
    private boolean showContentsPage = false;
    private boolean showFooterLine = false;
    private boolean showType = true;

    private String command;
    private String commandFlags;

    public DestinationList(List<Destination> destinations) {
        this.plugin = CraftBahnPlugin.plugin;
        this.destinations = destinations;
        this.pages = new ArrayList<>();
    }

    public void build() {
        int row = 0;
        Component page = Component.text("");

        if (this.showContentsPage)
            this.pages.add(Localization.COMMAND_DESTINATIONS_HEAD.deserialize());

        TreeMap<String, List<Destination>> serverMap = new TreeMap<>();
        for (Destination dest : this.destinations) {
            if (!serverMap.containsKey(dest.getServer()))
                serverMap.put(dest.getServer(), new ArrayList<>());

            serverMap.get(dest.getServer()).add(dest);
        }

        List<String> keys = new ArrayList<>(serverMap.keySet());
        List<String> sortedList = new ArrayList<>();

        String firstKey = null;
        String lastKey = null;

        for (String key : keys) {
            if (key.equalsIgnoreCase(CraftBahnPlugin.plugin.getServerName()))
                firstKey = key;
            else if (key.equalsIgnoreCase("creative"))
                lastKey = key;
            else
                sortedList.add(key);
        }

        if (firstKey != null) sortedList.add(0, firstKey);
        if (lastKey != null) sortedList.add(sortedList.size(), lastKey);

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
                    .append(Localization.COMMAND_DESTINATIONS_LIST_CAPTION.deserialize(PlaceholderResolver.resolver(
                        "server", capitalize(serverName))))
                    .append(Component.newline());

            row = row + 2;

            int items = 0;
            for (Destination dest : serverMap.get(serverName)) {
                row++;

                Component btnDestination;
                PlaceholderResolver placeholderResolver = PlaceholderResolver.resolver("destination", dest.getName());

                if (dest.getType() == Destination.DestinationType.PLAYER_STATION)
                    btnDestination = Localization.COMMAND_DESTINATIONS_LIST_ENTRY_PLAYER.deserialize(placeholderResolver);
                else
                    btnDestination = Localization.COMMAND_DESTINATIONS_LIST_ENTRY_OTHER.deserialize(placeholderResolver);

                Collection<Destination> duplicates = CraftBahnPlugin.plugin.getDestinationStorage().getDestinations(dest.getName());
                Component hoverText = Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_CAPTION.deserialize(
                        PlaceholderResolver.resolver("command", "/fahrziel " + dest.getName() + (duplicates.size() > 1 ? (" " + dest.getServer()) : "")));

                if (this.showType)
                    hoverText = hoverText.append(Component.newline()).append(Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_TYPE.deserialize(
                            PlaceholderResolver.resolver("type", dest.getType().toString())));

                if ((dest.getType().equals(Destination.DestinationType.PLAYER_STATION) || dest.getType().equals(Destination.DestinationType.PUBLIC_STATION)) && dest.getOwner() != null && this.showOwner) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(dest.getOwner());
                    String unkown = Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNERUNKOWN.get();
                    StringBuilder strOwner = new StringBuilder((owner.hasPlayedBefore() ? owner.getName() : unkown) + ", ");
                    for (UUID uuid : dest.getParticipants()) {
                        OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                        if (!participant.hasPlayedBefore()) continue;
                        strOwner.append(participant.getName()).append(", ");
                    }

                    hoverText = hoverText.append(Component.newline()).append(Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNER.deserialize(
                            PlaceholderResolver.resolver("owner", strOwner.substring(0, strOwner.length()-2))));
                }

                if (dest.getLocation() != null && this.showLocation) {
                    hoverText = hoverText.append(Component.newline()).append(Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_LOCATION.deserialize(
                            PlaceholderResolver.resolver("location", Math.round(dest.getLocation().getX()) + ", " + Math.round(dest.getLocation().getY()) + ", " + Math.round(dest.getLocation().getZ()))));
                    hoverText = hoverText.append(Component.newline()).append(Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_WORLD.deserialize(
                            PlaceholderResolver.resolver("world", dest.getWorld())));
                }

                btnDestination = btnDestination
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziel " + dest.getName() + (duplicates.size() > 1 ? dest.getServer() : "")))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

                // Append teleport-button
                if (dest.getLocation() != null && this.showLocation)
                    btnDestination = btnDestination.append(Localization.COMMAND_DESTINATIONS_BTN_TELEPORT.deserialize());

                page = page
                        .append(Localization.PREFIX.deserialize())
                        .append(btnDestination)
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

        if (pages.size() > 1) {
            output = output.append(Component.newline());

            Component btnBackwards;
            if (pageIndex > 1) {
                btnBackwards = Localization.COMMAND_DESTINATIONS_BTN_BACKWARDS_ON.deserialize(
                        PlaceholderResolver.resolver("command", this.command + this.commandFlags + " --page " + (pageIndex - 1)),
                        PlaceholderResolver.resolver("page", String.valueOf(pageIndex - 1)));
            } else
                btnBackwards = Localization.COMMAND_DESTINATIONS_BTN_BACKWARDS_OFF.deserialize();

            Component btnForwards;
            if (pageIndex < this.pages.size()) {
                btnForwards = Localization.COMMAND_DESTINATIONS_BTN_FORWARDS_ON.deserialize(
                        PlaceholderResolver.resolver("command", this.command + this.commandFlags + " --page " + (pageIndex + 1)),
                        PlaceholderResolver.resolver("page", String.valueOf(pageIndex + 1)));
            } else
                btnForwards = Localization.COMMAND_DESTINATIONS_BTN_FORWARDS_OFF.deserialize();

            output = output
                    .append(btnBackwards)
                    .append(Localization.COMMAND_DESTINATIONS_LIST_INDICATOR.deserialize(
                            PlaceholderResolver.resolver("actual", String.valueOf(pageIndex)),
                            PlaceholderResolver.resolver("total", String.valueOf(pages.size()))
                    ))
                    .append(btnForwards)
                    .append(Component.newline());
        }

        return output;
    }

    public void sendPage(Player player, int pageIndex) {
        // Build pages
        build();

        if (pageIndex < 1)
            Localization.COMMAND_DESTINATIONS_LIST_INVALIDPAGE.message(player);
        else if (pageIndex > getPageCount())
            Localization.COMMAND_DESTINATIONS_LIST_UNKOWNPAGE.message(player,
                    PlaceholderResolver.resolver("pages", String.valueOf(getPageCount())));
        else {
            Component message = renderPage(pageIndex);

            if (this.showFooterLine) {
                message = message
                        .append(Component.newline())
                        .append(Localization.FOOTER.deserialize());
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

    public void setPage(int i, Component page) {
        this.pages.set(i, page);
    }
    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }
    public void setCommand(String command) {
        this.command = command;
    }
    public void setCommandFlags(String commandFlags) {
        this.commandFlags = commandFlags;
    }

    public Component getPage(int i) {
        return this.pages.get(i);
    }
    public int getPageCount() {
        return pages.size();
    }

    public void showOwner(boolean show) { this.showOwner = show; }
    public void showLocation(boolean show) { this.showLocation = show; }
    public void showContentsPage(boolean show) {
        this.showContentsPage = show;
    }
    public void showFooterLine(boolean show) { this.showFooterLine = show; }
}
