package de.crafttogether.craftbahn.destinations;
import de.crafttogether.craftbahn.util.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class DestinationList {

    private List<Destination> destinations;
    private List<TextComponent> pages;
    private Destination.DestinationType filterType;

    private int itemsPerPage = 8;
    private boolean showOwner = false;
    private boolean showLocation = false;

    public DestinationList() {
        this.destinations = new ArrayList<>(DestinationStorage.getDestinations());
        this.pages = new ArrayList<>();
        this.filterType = null;
    }

    public void build() {
        TextComponent page = new TextComponent();
        int row = 0;

        if (this.filterType != null) Bukkit.getLogger().info("Applied Filter: + " + this.filterType.name());
        Bukkit.getLogger().info("Destinations: " + this.destinations.size());

        TreeMap<String, List<Destination>> serverMap = new TreeMap<>();

        for (Destination dest : this.destinations) {
            if (this.filterType != null && !dest.getType().equals(this.filterType))
                continue;

            if (!serverMap.containsKey(dest.getServer()))
                serverMap.put(dest.getServer(), new ArrayList<>());

            serverMap.get(dest.getServer()).add(dest);
        }

        for (String serverName : serverMap.keySet()) {
            if ((this.itemsPerPage - row) < 3) {
                // New Page
                this.pages.add(page);
                page = new TextComponent();
                row = 0;
            }

            if (row == 0 && pages.size() < 1)
                page.addExtra(Message.newLine());

            page.addExtra(Message.format("&7# &6&l" + capitalize(serverName) + ":"));
            page.addExtra(Message.newLine());

            row++;

            for (Destination dest : serverMap.get(serverName)) {
                row++;

                TextComponent btnFahrziel;
                if (dest.getType() == Destination.DestinationType.PLAYER_STATION)
                    btnFahrziel = Message.format("&8» &e" + dest.getName());
                else
                    btnFahrziel = Message.format("&8» &6" + dest.getName());

                String hoverText = "/fahrziel " + dest.getName();
                if ((dest.getType().equals(Destination.DestinationType.PLAYER_STATION) || dest.getType().equals(Destination.DestinationType.PUBLIC_STATION)) && dest.getOwner() != null && this.showOwner) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(dest.getOwner());

                    String strOwner = (owner.hasPlayedBefore() ? owner.getName() : "Unbekannt") + ", ";
                    for (UUID uuid : dest.getParticipants()) {
                        OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
                        if (!participant.hasPlayedBefore()) continue;
                        strOwner += participant.getName() + ", ";
                    }

                    hoverText += "\n&6Besitzer: &e" + strOwner.substring(0, strOwner.length()-2);
                }

                if (dest.getLocation() != null && this.showLocation) {
                    hoverText += "\n&6Koordinaten: &e" + Math.round(dest.getLocation().getX()) + ", " + Math.round(dest.getLocation().getY()) + ", " + Math.round(dest.getLocation().getZ());
                    hoverText += "\n&6Welt: &e" + dest.getWorld();
                }

                btnFahrziel.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/fahrziel " + dest.getName()));
                btnFahrziel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format(hoverText))).create()));

                if (dest.getLocation() != null && this.showLocation) {
                    TextComponent tpBtn = new TextComponent();
                    tpBtn.addExtra(Message.format(" &7[&fTP&7]"));
                    tpBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrzieledit tp " + dest.getName()));
                    tpBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&6Teleportiere zum Zielort"))).create()));
                    btnFahrziel.addExtra(tpBtn);
                }

                page.addExtra(btnFahrziel);
                page.addExtra(Message.newLine());

                if (row >= this.itemsPerPage) {
                    // New Page
                    this.pages.add(page);
                    page = new TextComponent();
                    row = 0;
                }
            }
        }

        this.pages.add(page);
    }

    public TextComponent renderPage(int pageIndex) {
        if (this.pages.size() < 1)
            return null;

        TextComponent output = new TextComponent();
        TextComponent page = this.pages.get(pageIndex - 1);

        if (pageIndex > 1)
            output.addExtra(Message.newLine());

        output.addExtra(page);
        output.addExtra(Message.newLine());

        TextComponent btnPrevious;
        if (pageIndex > 1) {
            btnPrevious = Message.format("&a----<< &6Zurück");
            btnPrevious.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele " + (filterType == null ? "" : filterType.name() + " ") + (pageIndex - 1)));
            btnPrevious.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (
                new ComponentBuilder(Message.format("&6Vorherige Seite: &e" + (pageIndex - 1)))
            ).create()));
        }
        else
            btnPrevious = Message.format("&2----<< &7Zurück");

        output.addExtra(btnPrevious);

        output.addExtra(Message.format(" &2" + pageIndex +  "&7/&2" + pages.size() + " "));

        TextComponent btnForward;
        if (pageIndex < this.pages.size()) {
            btnForward = Message.format("&6Nächste &2>>----");
            btnForward.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele " + (filterType == null ? "" : filterType.name() + " ") + (pageIndex + 1)));
            btnForward.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (
                new ComponentBuilder(Message.format("&6Nächste Seite: &e" + (pageIndex + 1)))
            ).create()));
        }
        else
            btnForward = Message.format("&8Weiter &6>>");

        output.addExtra(btnForward);

        return output;
    }

    public void setPage(int i, TextComponent page) {
        this.pages.set(i, page);
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public void setFilterType(Destination.DestinationType filterType) {
        this.filterType = filterType;
    }

    public TextComponent getPage(int i) {
        return this.pages.get(i);
    }

    public int getPageCount() {
        return pages.size();
    }

    public void getBook(Player player) {
        if (itemsPerPage > 8) // Set maximum items per book page
            itemsPerPage = 8;

        build();
    }

    public void sendPage(Player player, int pageIndex) {
        // Build pages
        build();

        if (pageIndex < 1) {
            player.sendMessage(Message.format("&6CraftBahn &8» &cUngültige Seitennummer."));
            return;
        }

        if (pageIndex == 0 || pageIndex > getPageCount()) {
            player.sendMessage(Message.format("&6CraftBahn &8» &cEs gibt nur " + getPageCount() + " Seite" + (getPageCount() > 1 ? "n":"")));
            return;
        }

        player.sendMessage(renderPage(pageIndex));
    }

    private String capitalize(String name) {
        String firstLetter = name.substring(0, 1);
        String remainingLetters = name.substring(1);
        firstLetter = firstLetter.toUpperCase();
        return firstLetter + remainingLetters;
    }

    public void setOwnerVisible(boolean show) {
        this.showOwner = show;
    }

    public void setLocationVisible(boolean show) {
        this.showLocation = show;
    }
}
