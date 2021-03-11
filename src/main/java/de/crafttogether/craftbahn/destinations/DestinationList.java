package de.crafttogether.craftbahn.destinations;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DestinationList {

    private List<Destination> destinations;
    private List<TextComponent> pages;
    private Destination.DestinationType filterType;

    private int itemsPerPage = 8;

    public DestinationList(Collection destinations) {
        this.destinations = (List<Destination>) destinations;
        pages = new ArrayList<>();
    }

    private void build() {

        for (Destination dest : destinations) {
            // Man da haste dir ja wieder ein Ei gelegt eh -.-
        }

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

    public void getBook(Player player) {
        if (itemsPerPage > 8) // Set maximum items per book page
            itemsPerPage = 8;

        build();
    }

    public void sendTo(Player player) {
        build();
    }

    public void sendTo(ArrayList<Player> players) {

    }


}
