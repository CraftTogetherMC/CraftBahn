package de.crafttogether.craftbahn;

import cloud.commandframework.captions.Caption;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.localization.LocalizationEnum;
import org.bukkit.ChatColor;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("command.prefix", "<gold>CraftBahn </gold><dark_gray>» </dark_gray>");
    public static final Localization HEADER = new Localization("command.header", "<yellow>--------------</yellow> <red><bold>CraftBahn</bold></red> <yellow>--------------<yellow/>");

    public static final Localization COMMAND_NOPERM = new Localization("command.noperm", ChatColor.DARK_GRAY + "You do not have permission, ask an admin to do this for you.");

    public static final Localization COMMAND_DESTINATION_NOTEXIST = new Localization("command.destination.notexist", "<prefix><red>Es wurde kein Ziel mit dem Namen <gold><input/></gold> gefunden.</red>");
    public static final Localization COMMAND_DESTINATION_NOPERMISSION = new Localization("command.destination.nopermission", "<prefix><red>Auf dieses Ziel hast du keinen Zugriff.</red>");
    public static final Localization COMMAND_DESTINATION_NOTRAIN = new Localization("command.destination.notrain", "<prefix><red>Bitte setze dich zuerst in einen Zug.</red>");
    public static final Localization COMMAND_DESTINATION_MULTIPLEDEST = new Localization("command.destination.multipledest", "<prefix><red>Es wurden mehrere mögliche Ziele gefunden.</red>");
    public static final Localization COMMAND_DESTINATION_APPLIED = new Localization("command.destination.applied", "<prefix><yellow>Dieser Zug versucht nun das Ziel <gold><destination/></gold> zu erreichen.</yellow>");
    public static final Localization COMMAND_DESTINATION_INFO = new Localization("command.destination.info",
            """
                    --------------- <red>CraftBahn</red> ---------------
                    
                    <prefix/><gold>Willkommen bei der CraftBahn!</gold>
                    <prefix/><yellow>Unser Schienennetz erstreckt sich</yellow>
                    <prefix/><yellow>In alle Himmelsrichtungen.</yellow>

                    <prefix/><gold><bold>Anleitung:</bold></gold>
                    <prefix/><click:run_command:/bahnhof><red>/bahnhof</red></click>

                    <prefix/><gold><bold>Fahrziele wählen:</bold></gold>
                    <prefix/><click:run_command:/fahrziel><red>/fahrziel</red></click>
                    <prefix/><yellow>oder</yellow>
                    <prefix/><click:suggest_command:/fahrziel NAME><red>/fahrziel</red></click> <gray>\\<name></gray>

                    <prefix/><yellow>Gute Fahrt!</yellow>""");

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public String get() {
        return CraftBahnPlugin.plugin.getLocalizationManager().getLocale(this.getName());
    }

    public Caption getCaption() {
        return Caption.of(getName());
    }
}
