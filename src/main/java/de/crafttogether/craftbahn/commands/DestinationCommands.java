package de.crafttogether.craftbahn.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Quoted;
import de.crafttogether.CraftBahnPlugin;
import org.bukkit.command.CommandSender;

public class DestinationCommands {
    @CommandMethod("fahrziele <stationType>")
    @CommandDescription("Zeigt eine Liste mit möglichen Fahrzielen.")
    public void fahrziele(final CraftBahnPlugin plugin, final CommandSender sender, final @Quoted @Argument("stationType") String stationType) {
        sender.sendMessage("Test");
    }

    @CommandMethod("fahrzieledit setowner")
    @CommandDescription("Ändert den angegebenen Besitzer eines Fahrziel")
    public void fahrzieledit_setowner(final CraftBahnPlugin plugin, final CommandSender sender) {
        sender.sendMessage("Test");
    }
}
