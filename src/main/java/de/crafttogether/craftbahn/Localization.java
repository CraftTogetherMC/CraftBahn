package de.crafttogether.craftbahn;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.localization.LocalizationEnum;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("command.prefix", "<gold>CraftBahn </gold><dark_gray>» </dark_gray>");
    public static final Localization HEADER = new Localization("command.header", "<yellow>--------------</yellow> <red><bold>CraftBahn</bold></red> <yellow>--------------<yellow/>");
    public static final Localization FOOTER = new Localization("command.footer", "<yellow>----------------------------------------</yellow>");

    public static final Localization COMMAND_DESTINATION_NOTEXIST = new Localization("command.destination.notexist", "<prefix/><red>Es wurde kein Ziel mit dem Namen <gold>{input}</gold> gefunden.</red>");
    public static final Localization COMMAND_DESTINATION_NOPERMISSION = new Localization("command.destination.nopermission", "<prefix/><red>Auf dieses Ziel hast du keinen Zugriff.</red>");
    public static final Localization COMMAND_DESTINATION_NOTRAIN = new Localization("command.destination.notrain", "<prefix/><red>Bitte setze dich zuerst in einen Zug.</red>");
    public static final Localization COMMAND_DESTINATION_MULTIPLEDEST = new Localization("command.destination.multipledest", "<prefix/><red>Es wurden mehrere mögliche Ziele gefunden.</red>");
    public static final Localization COMMAND_DESTINATION_APPLIED = new Localization("command.destination.applied", "<prefix/><yellow>Dieser Zug versucht nun das Ziel <gold>{destination}</gold> zu erreichen.</yellow>");
    public static final Localization COMMAND_DESTINATION_INFO = new Localization("command.destination.info",
            """
                <header/>
                
                <hover:show_text:'<green>/bahnhof</green>'>
                <prefix/><gold>Willkommen bei der CraftBahn!</gold>
                <prefix/><yellow>Unser Schienennetz erstreckt sich</yellow>
                <prefix/><yellow>In alle Himmelsrichtungen.</yellow>

                <prefix/><gold><bold>Anleitung:</bold></gold>
                <prefix/><click:run_command:/bahnhof><red>/bahnhof</red></click>
                </hover>

                <prefix/><gold><bold>Fahrziele wählen:</bold></gold>
                <prefix/><click:run_command:/fahrziel><red>/fahrziel</red></click>
                <prefix/><yellow>oder</yellow>
                <prefix/><click:suggest_command:/fahrziel NAME><red>/fahrziel</red></click> <gray>\\<name></gray>

                <prefix/><yellow>Gute Fahrt!</yellow>
                
                <footer/>""");

    public static final Localization COMMAND_DESTINATIONS_HEAD = new Localization("command.destinations.head",
            """
                <header/>
                
                <hover:show_text:'<green>/bahnhof</green>'>
                <click:run_command:/bahnhof>
                <prefix/><yellow>Guten Tag, Reisender!</yellow>
                <prefix/><yellow>Verstehst du nur </yellow>
                <prefix/><red>/bahnhof</red><yellow>?</yellow>
                </click>
                </hover>
                
                <green>» </green><yellow>Hauptbahnhöfe</yellow>
                <hover:show_text:'<green>/fahrziele Hauptbahnhöfe</green>'><click:run_command:/fahrziele Hauptbahnhöfe><red>/fahrziel</red></click></hover>
                <green>» </green><yellow>Bahnhöfe</yellow>
                <hover:show_text:'<green>/fahrziele Bahnhöfe</green>'><click:run_command:/fahrziele Bahnhöfe><red>/fahrziel</red></click></hover>
                <green>» </green><yellow>Spielerbahnhöfe</yellow>
                <hover:show_text:'<green>/fahrziele Spielerbahnhöfe</green>'><click:run_command:/fahrziele Spielerbahnhöfe><red>/fahrziel</red></click></hover>
                <green>» </green><yellow>Öffentliche Ziele</yellow>
                <hover:show_text:'<green>/fahrziele Öffentlich</green>'><click:run_command:/fahrziele Öffentlich><red>/fahrziel</red></click></hover>""");
    public static final Localization COMMAND_DESTINATIONS_LIST_INVALIDPAGE = new Localization("command.destinations.list.invalidpage", "<prefix/><red>Ungültige Seitennummer.</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_UNKOWNPAGE = new Localization("command.destinations.list.unkownpage", "<prefix/><red>Es gibt nur {pages} Seite(n).</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_INDICATOR = new Localization("command.destinations.list.indicator", " <green>{actual} <green>/<green> <green>{total}</green> ");
    public static final Localization COMMAND_DESTINATIONS_LIST_CAPTION = new Localization("command.destinations.list.caption", "<prefix/><gray> # </gray><gold><bold>{server}</bold></gold>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_PLAYER = new Localization("command.destinations.list.entry.player", "<yellow>{destination}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_OTHER = new Localization("command.destinations.list.entry.other", "<gold>{destination}</gold>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_CAPTION = new Localization("command.destinations.list.entry.hover.caption", "<green>{command}</green>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_TYPE = new Localization("command.destinations.list.entry.hover.type", "<gold>Stations-Typ: </gold><yellow>{type}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNER = new Localization("command.destinations.list.entry.hover.owner", "<gold>Besitzer: </gold><yellow>{owner}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNER_UNKOWN = new Localization("command.destinations.list.entry.hover.owner.unkown", "Unbekannt");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_LOCATION = new Localization("command.destinations.list.entry.hover.location", "<gold>Koordinaten: </gold><yellow>{location}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_WORLD = new Localization("command.destinations.list.entry.hover.world", "<gold>Welt: </gold><yellow>{world}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_BTN_FORWARDS_ON = new Localization("command.destinations.btn.forwards.on", "<hover:show_text:'<green>Nächste Seite</green>'><click:run_command:{command}><gold>Weiter</gold> <green>>>----</green></click></hover>");
    public static final Localization COMMAND_DESTINATIONS_BTN_FORWARDS_OFF = new Localization("command.destinations.btn.forwards.off", "<gray>Weiter</gray> <green>>>----</green>");
    public static final Localization COMMAND_DESTINATIONS_BTN_BACKWARDS_ON = new Localization("command.destinations.btn.backwards.on", "<hover:show_text:'<green>Vorherige Seite</green>'><click:run_command:{command}><green>----<<</green> <gold>Zurück</gold></click></hover>");
    public static final Localization COMMAND_DESTINATIONS_BTN_BACKWARDS_OFF = new Localization("command.destinations.btn.backwards.off", "<green>----<<</green> <gray>Zurück</gray>");
    public static final Localization COMMAND_DESTINATIONS_BTN_TELEPORT = new Localization("command.destinations.btn.teleport", "<hover:show_text:'<green>Zum Bahnhof teleportieren</green>'><click:run_command:{command}> <gray>[</gray><white>TP</white><gray>]</gray></click></hover>");

    public static final Localization COMMAND_NOPERM = new Localization("command.noperm", "<red>Dazu hast du keine Berechtigung</red>");

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public String get() {
        return CraftBahnPlugin.plugin.getLocalizationManager().getLocale(this.getName());
    }
}
