package de.crafttogether.craftbahn.commands;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import de.crafttogether.CraftBahnPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Commands {
    private final CloudSimpleHandler cloud = new CloudSimpleHandler();

    // Command handlers
    private final FahrzielCommands commands_cart = new FahrzielCommands();

    public CloudSimpleHandler getHandler() {
        return cloud;
    }

    public void enable(CraftBahnPlugin plugin) {
        cloud.enable(plugin);
        cloud.annotations(this);

        plugin.getLogger().info("CLOUD ENABLED");
    }

    @CommandMethod("cbversion")
    @CommandDescription("Test Command")
    public void cbversion(final CraftBahnPlugin plugin, final CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "CraftBahn-Version LOL: " + plugin.getDescription().getVersion());
    }
}