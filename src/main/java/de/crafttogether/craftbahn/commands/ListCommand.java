package de.crafttogether.craftbahn.commands;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListCommand implements TabExecutor {
    private CraftBahn plugin = CraftBahn.getInstance();

    public boolean onCommand(CommandSender sender, Command cmd, String st, String[] args) {
        if (cmd.getName().equalsIgnoreCase("fahrziele")) {
            if (sender instanceof Player) {
                Player p = Bukkit.getPlayer(((Player) sender).getUniqueId());
                Destination.DestinationType filterType = null;
                int pageIndex = 1;

                if (args.length > 0) {
                    String _type = args[0].replace("höfe", "hof");
                    filterType = Destination.findType(_type);

                    if (filterType == null) {
                        try {
                            filterType = Destination.DestinationType.valueOf(_type);
                        } catch (Exception exception) { }
                    }

                    try {
                        if (filterType == null)
                            pageIndex = Integer.parseInt(args[0]);
                        else
                            pageIndex = Integer.parseInt(args[1]);
                    } catch (Exception exception) { }
                }

                DestinationList list = new DestinationList();
                list.setFilterType(filterType);
                list.suggestCommands(true);
                list.showOwner(true);
                list.showLocation(true);
                list.sendPage(p, pageIndex);
            } else {
                Bukkit.getLogger().info("Dieser Befehl kann nicht von der Konsole ausgeführt werden.");
                return true;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<String> proposals = new ArrayList<>();

        for (Destination.DestinationType type : Destination.DestinationType.values())
            proposals.add(type.toString().replace("hof", "höfe"));

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