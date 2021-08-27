package de.crafttogether.craftbahn.commands;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationList;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListCommand implements TabExecutor {
    private CraftBahnPlugin plugin = CraftBahnPlugin.getInstance();

    public boolean onCommand(CommandSender sender, Command cmd, String st, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setdestination")) {
            if (!(sender instanceof Player))
                return true;

            Player p = (Player) sender;
            MinecartGroup train = TCHelper.getTrain(p);

            if (train == null) {
                p.sendMessage(Message.format("&6CraftBahn &8» &cBitte setze dich zuerst in einen Zug."));
                return true;
            }

            train.getProperties().setDestination(args[0]);
        }

        if (cmd.getName().equalsIgnoreCase("setroute")) {
            if (!(sender instanceof Player))
                return true;

            Player p = (Player) sender;
            MinecartGroup train = TCHelper.getTrain(p);

            if (train == null) {
                p.sendMessage(Message.format("&6CraftBahn &8» &cBitte setze dich zuerst in einen Zug."));
                return true;
            }

            List<String> route = new ArrayList<>();
            String destination = null;
            for (int i = 0; i < args.length; i++) {
                if (i == 0)
                    destination = args[i];
                else
                    route.add(args[i]);
            }

            if (route.size() > 0)
                train.getProperties().setDestinationRoute(route);

            train.getProperties().setDestination(destination);
        }

        if (cmd.getName().equalsIgnoreCase("fahrziele")) {
            if (sender instanceof Player) {
                Player p = Bukkit.getPlayer(((Player) sender).getUniqueId());
                Destination.DestinationType filterType = null;
                Integer pageIndex = null;

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
                list.showContents(false);
                list.suggestCommands(true);
                list.showOwner(true);
                list.showLocation(true);
                list.showFooter(true);
                list.showContents(true);

                List<String> argList = Arrays.asList(args);

                if (pageIndex == null)
                    pageIndex = (filterType == null) ? 1 : 2;

                if (argList.contains("--book")) {
                    CraftBahnPlugin.getInstance().getLogger().info("OPEN BOOK");
                    ItemStack book = list.getBook();
                    p.getInventory().setItem(0, book);
                    //Message.openBook(list.getBook(), p);
                }
                else
                    list.sendPage(p, pageIndex);

            } else {
                CraftBahnPlugin.getInstance().getLogger().info("Dieser Befehl kann nicht von der Konsole ausgeführt werden.");
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