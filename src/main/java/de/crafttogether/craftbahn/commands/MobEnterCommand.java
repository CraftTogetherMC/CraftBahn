package de.crafttogether.craftbahn.commands;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MobEnterCommand implements TabExecutor {
    private CraftBahn plugin = CraftBahn.getInstance();

    public boolean onCommand(CommandSender sender, Command cmd, String st, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mobenter")) {
            if (!(sender instanceof Player))
                return true;

            Player p = (Player) sender;
            MinecartGroup train = TCHelper.getTrain(p);

            if (train == null) {
                p.sendMessage(Message.format("&6CraftBahn &8» &cBitte setze dich zuerst in einen Zug."));
                return true;
            }

            Location center = p.getLocation();
            int entered = 0;
            for (Entity entity : WorldUtil.getNearbyEntities(center, 5.0, 1.0, 5.0)) {
                if (entity.getVehicle() != null)
                    continue;

                if (EntityUtil.isMob(entity)) {
                    for (MinecartMember<?> member : train) {
                        if (member.getAvailableSeatCount(entity) > 0 && member.addPassengerForced(entity)) {
                            entered++;
                            break;
                        }
                    }
                }
            }

            if (entered > 0)
                p.sendMessage(Message.format("&6CraftBahn &8» &6Es wurden &e" + entered + " Tiere &6in deinen Zug gesetzt."));
            else
                p.sendMessage(Message.format("&6CraftBahn &8» &cEs wurden keine Tiere in einem Umkreis von 5 Blöcken zu dir gefunden"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        ArrayList<String> newList = new ArrayList<>();
        ArrayList<String> proposals = new ArrayList<>();

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