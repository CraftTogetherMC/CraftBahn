package de.crafttogether.craftbahn.destinations;
import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.Message;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class DestinationList {

    private final List<Destination> destinations;
    private final List<TextComponent> pages;
    private Destination.DestinationType filterType;
    private String filterName;

    private int itemsPerPage = 8;
    private boolean showOwner = false;
    private boolean showLocation = false;
    private boolean suggestCommands = false;
    private boolean showContents = false;
    private boolean showFooter = false;
    private boolean showType = true;

    public DestinationList() {
        this.destinations = new ArrayList<>(CraftBahn.getInstance().getDestinationStorage().getDestinations());
        this.pages = new ArrayList<>();
        this.filterType = null;
        this.filterName = null;
    }

    public DestinationList(List<Destination> destinations) {
        this.destinations = destinations;
        this.pages = new ArrayList<>();
        this.filterType = null;
    }

    public TextComponent getContentsPage() {
        TextComponent contents = new TextComponent();

        TextComponent info = Message.format("&e-------------- &c&lCraftBahn &e--------------");
        info.addExtra(Message.newLine());
        info.addExtra(Message.newLine());
        info.addExtra(Message.format("&6CraftBahn &8» &eGuten Tag, Reisender!"));
        info.addExtra(Message.newLine());
        info.addExtra(Message.format("&6CraftBahn &8» &eVerstehst du nur "));
        info.addExtra(Message.format("&c/bahnhof&e?"));
        info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bahnhof"));
        info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&2Informationen zum Schienennetz"))).create()));

        contents.addExtra(info);
        contents.addExtra(Message.newLine());

        contents.addExtra(Message.format("&6CraftBahn &8»"));
        contents.addExtra(Message.newLine());
        contents.addExtra(Message.format("&6CraftBahn &8» &6&lMögliche Fahrziele:"));
        contents.addExtra(Message.newLine());
        contents.addExtra(Message.format("&6CraftBahn &8»"));
        contents.addExtra(Message.newLine());

        TextComponent btnMainStations = Message.format("&6CraftBahn &8» &2> &eHauptbahnhöfe");
        btnMainStations.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele MAIN_STATION 2"));
        btnMainStations.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&2Hauptbahnhöfe"))).create()));
        contents.addExtra(btnMainStations);
        contents.addExtra(Message.newLine());

        TextComponent btnStations = Message.format("&6CraftBahn &8» &2> &eSystem-Bahnhöfe");
        btnStations.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele STATION 2"));
        btnStations.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&2Bahnhöfe"))).create()));
        contents.addExtra(btnStations);
        contents.addExtra(Message.newLine());

        TextComponent btnPublicStations = Message.format("&6CraftBahn &8» &2> &eÖffentliche Bahnhöfe");
        btnPublicStations.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele PUBLIC_STATION 2"));
        btnPublicStations.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&2Öffentlich"))).create()));
        contents.addExtra(btnPublicStations);
        contents.addExtra(Message.newLine());

        TextComponent btnPlayerStations = Message.format("&6CraftBahn &8» &2> &eSpieler-Bahnhöfe");
        btnPlayerStations.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele PLAYER_STATION 2"));
        btnPlayerStations.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&2Spielerbahnhöfe"))).create()));
        contents.addExtra(btnPlayerStations);
        contents.addExtra(Message.newLine());

        return contents;
    }

    public void build() {
        TextComponent page = new TextComponent();
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
            Message.debug(key + " - " + CraftBahn.getInstance().getServerName());
            if (key.equalsIgnoreCase(CraftBahn.getInstance().getServerName()))
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

        for (String bla : sortedList) {
            Message.debug("SORTED: ");
            Message.debug(bla);
        }

        for (String serverName : sortedList) {
            if ((this.itemsPerPage - row) < 4) {
                // New Page
                this.pages.add(page);
                page = new TextComponent();
                row = 0;
            }

            if (row != 0) {
                page.addExtra(Message.newLine());
                row++;
            }

            page.addExtra(Message.format("&6CraftBahn &8» &7# &6&l" + capitalize(serverName) + ":"));
            page.addExtra(Message.newLine());

            row = row + 2;

            int items = 0;
            for (Destination dest : serverMap.get(serverName)) {
                row++;

                TextComponent btnFahrziel;
                if (dest.getType() == Destination.DestinationType.PLAYER_STATION)
                    btnFahrziel = Message.format("&6CraftBahn &8» &e" + dest.getName());
                else
                    btnFahrziel = Message.format("&6CraftBahn &8» &6" + dest.getName());

                Collection<Destination> duplicates = CraftBahn.getInstance().getDestinationStorage().getDestinations(dest.getName());

                String hoverText = "&2/fahrziel " + dest.getName() + (duplicates.size() > 1 ? (" &7" + dest.getServer()) : "");

                if (this.showType)
                    hoverText += "\n&6Stations-Typ: &e" + dest.getType().toString();

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

                if (this.suggestCommands)
                    btnFahrziel.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/fahrziel " + dest.getName() + (duplicates.size() > 1 ? (" " + dest.getServer()) : "")));
                else
                    btnFahrziel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziel " + dest.getName() + (duplicates.size() > 1 ? (" " + dest.getServer()) : "")));

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

                items++;

                if (row >= this.itemsPerPage && serverMap.get(serverName).size() > items) {
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

        output.addExtra(Message.newLine());
        output.addExtra(page);

        String filter = (filterType == null ? "" : filterType.name() + " ");
        filter = (filterName == null ? filter : filterName + " ");

        String command = "/fahrziele";

        if (filterName != null)
            command = "/fahrziel";

        if (pages.size() > 1) {
            output.addExtra(Message.newLine());
            TextComponent btnPrevious;
            if (pageIndex > 1) {
                btnPrevious = Message.format("&a----<< &6Zurück");
                btnPrevious.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + filter + (pageIndex - 1)));
                btnPrevious.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (
                        new ComponentBuilder(Message.format("&6Vorherige Seite: &e" + (pageIndex - 1)))
                ).create()));
            } else
                btnPrevious = Message.format("&2----<< &7Zurück");

            output.addExtra(btnPrevious);

            output.addExtra(Message.format(" &2" + pageIndex + "&7/&2" + pages.size() + " "));

            TextComponent btnForward;
            if (pageIndex < this.pages.size()) {
                btnForward = Message.format("&6Weiter &2>>----");
                btnForward.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + filter + (pageIndex + 1)));
                btnForward.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (
                        new ComponentBuilder(Message.format("&6Nächste Seite: &e" + (pageIndex + 1)))
                ).create()));
            } else
                btnForward = Message.format("&7Weiter >>----");

            output.addExtra(btnForward);
            output.addExtra(Message.newLine());
        }

        return output;
    }

    public void setPage(int i, TextComponent page) {
        this.pages.set(i, page);
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public void setFilterType(Destination.DestinationType destinationType) {
        this.filterType = destinationType;
    }

    public void setFilterName(String destinationName) { this.filterName = destinationName; }

    public TextComponent getPage(int i) {
        return this.pages.get(i);
    }

    public int getPageCount() {
        return pages.size();
    }

    public ItemStack getBook() {
        if (itemsPerPage > 8) // Set maximum items per book page
            itemsPerPage = 8;

        // Suggesting commands don't work in books
        suggestCommands(false);

        // Generate pages
        build();

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        for (TextComponent page : this.pages) {
            BaseComponent[] component = new ComponentBuilder().append(page).create();
            bookMeta.spigot().addPage(component);
        }

        bookMeta.setTitle("Fahrziele");
        bookMeta.setAuthor("CraftBahn");
        book.setItemMeta(bookMeta);

        return book;
    }

    public void sendPage(Player player, int pageIndex) {
        // Build pages
        build();

        if (pageIndex < 1) {
            player.sendMessage(Message.format("&6CraftBahn &8» &cUngültige Seitennummer."));
            return;
        }

        if (pageIndex > getPageCount()) {
            player.sendMessage(Message.format("&6CraftBahn &8» &cEs gibt nur " + getPageCount() + " Seite" + (getPageCount() > 1 ? "n":"")));
            return;
        }

        TextComponent message = renderPage(pageIndex);

        if (this.showFooter) {
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&e----------------------------------------"));
        }

        player.sendMessage(message);
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

    public void suggestCommands(boolean suggest) {
        this.suggestCommands = suggest;
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
