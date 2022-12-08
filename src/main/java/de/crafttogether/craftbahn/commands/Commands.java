package de.crafttogether.craftbahn.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.commands.annotations.CommandRequiresPermission;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {
    private static final CloudSimpleHandler cloud = new CloudSimpleHandler();

    public void enable(CraftBahnPlugin plugin) {
        cloud.enable(plugin);

        // Command handlers
        DestinationCommands commands_destination = new DestinationCommands(cloud);

        // Suggestions
        cloud.suggest("onlinePlayers", (context, input) -> {
            List<String> result = Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return result;
        });

        cloud.suggest("serverName", (context, input) -> {
            List<String> result =  plugin.getDestinationStorage().getDestinations().stream().distinct()
                    .map(Destination::getServer)
                    .collect(Collectors.toList());
            return result;
        });

        cloud.suggest("destinationName", (context, input) -> {
            List<String> result =  plugin.getDestinationStorage().getDestinations().stream()
                    .map(Destination::getName)
                    .collect(Collectors.toList());
            return result;
        });

        cloud.suggest("destinationType", (context, input) -> {
            List<String> result =  Arrays.stream(Destination.DestinationType.values())
                    .map(Destination.DestinationType::toString)
                    .collect(Collectors.toList());
            return result;
        });

        // Register Annotations
        cloud.annotations(this);
        cloud.annotations(commands_destination);
    }

    @CommandMethod("craftbahn")
    @CommandDescription("Zeigt die aktuelle Version des Plugin")
    public void craftbahn(
            final CommandSender sender
    ) {
        sender.sendMessage(ChatColor.GREEN + "CraftBahn-Version: " + CraftBahnPlugin.plugin.getDescription().getVersion());
    }

    @CommandMethod(value="mobenter [radius]", requiredSender=Player.class)
    @CommandDescription("Lässt Tiere in der nahen Umgebung in den ausgewählten Zug einsteigen.")
    @CommandPermission("craftbahn.command.mobenter")
    public void mobenter(
            final Player sender,
            @Argument(value="radius") @Range(min = "1", max = "16") Integer radius
    ) {
        MinecartGroup group = TCHelper.getTrain(sender);

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(sender);
            return;
        }

        if (radius == null)
            radius = 8;

        Location center = sender.getLocation();
        int entered = 0;
        for (Entity entity : WorldUtil.getNearbyEntities(center, radius, radius, radius)) {
            if (entity.getVehicle() != null)
                continue;

            if (EntityUtil.isMob(entity)) {
                for (MinecartMember<?> member : group) {
                    if (member.getAvailableSeatCount(entity) > 0 && member.addPassengerForced(entity)) {
                        entered++;
                        break;
                    }
                }
            }
        }

        if (entered > 0)
            Localization.COMMAND_MOBENTER_SUCCESS.message(sender,
                    PlaceholderResolver.resolver("amount", String.valueOf(entered)));
        else
            Localization.COMMAND_MOBENTER_FAILED.message(sender,
                    PlaceholderResolver.resolver("radius", String.valueOf(radius)));
    }

    @CommandMethod(value="mobeject", requiredSender=Player.class)
    @CommandDescription("Lässt alle Tiere aus dem ausgewählten Zug aussteigen.")
    @CommandPermission("craftbahn.command.mobeject")
    public void mobeject(
            final Player sender
    ) {
        MinecartGroup group = TCHelper.getTrain(sender);

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(sender);
            return;
        }

        for (MinecartMember<?> member : group) {
            if (!member.getEntity().hasPassenger()) continue;

            for (Entity passenger : member.getEntity().getPassengers()) {
                if (EntityUtil.isMob(passenger))
                    member.eject();
            }
        }

        Localization.COMMAND_MOBEJECT_SUCCESS.message(sender);
    }

    public static CloudSimpleHandler getCloud() {
        return cloud;
    }

    public CommandManager getManager() {
        return cloud.getManager();
    }
}