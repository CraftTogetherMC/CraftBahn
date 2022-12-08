package de.crafttogether.craftbahn;

import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.localization.LocalizationEnum;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("command.prefix", "<gold>CraftBahn </gold><dark_gray>» </dark_gray>");
    public static final Localization HEADER = new Localization("command.header", "<yellow>--------------</yellow> <red><bold>CraftBahn</bold></red> <yellow>--------------<yellow/>");
    public static final Localization FOOTER = new Localization("command.footer", "<yellow>----------------------------------------</yellow>");

    public static final Localization COMMAND_NOPERM = new Localization("command.noperm", "<red>Dazu hast du keine Berechtigung</red>");
    public static final Localization COMMAND_NOTRAIN = new Localization("command.notrain", "<prefix/><red>Bitte setze dich zuerst in einen Zug.</red>");

    public static final Localization DESTINATIONTYPE_ALL = new Localization("destinationtype.all", "Alle");
    public static final Localization DESTINATIONTYPE_STATION = new Localization("destinationtype.station", "Bahnhof");
    public static final Localization DESTINATIONTYPE_MAIN_STATION = new Localization("destinationtype.main_station", "Hauptbahnhof");
    public static final Localization DESTINATIONTYPE_PLAYER_STATION = new Localization("destinationtype.player_station", "Spielerbahnhof");
    public static final Localization DESTINATIONTYPE_PUBLIC_STATION = new Localization("destinationtype.public_station", "Öffentlich");

    public static final Localization COMMAND_DESTINATION_NOTEXIST = new Localization("command.destination.notexist", "<prefix/><red>Es wurde kein Ziel mit dem Namen <gold>{input}</gold> gefunden.</red>");
    public static final Localization COMMAND_DESTINATION_NOPERMISSION = new Localization("command.destination.nopermission", "<prefix/><red>Auf dieses Ziel hast du keinen Zugriff.</red>");
    public static final Localization COMMAND_DESTINATION_MULTIPLEDEST = new Localization("command.destination.multipledest", "<prefix/><red>Es wurden mehrere mögliche Ziele gefunden.</red>");
    public static final Localization COMMAND_DESTINATION_APPLIED = new Localization("command.destination.applied", "<prefix/><yellow>Dieser Zug versucht nun das Ziel <gold>{destination}</gold> zu erreichen.</yellow>");
    public static final Localization COMMAND_DESTINATION_INFO = new Localization("command.destination.info",
            """
                <header/>

                <hover:show_text:'<green>Informationen zum Schienennetz</green>'><prefix/><gold>Willkommen bei der CraftBahn!</gold>
                <prefix/><yellow>Unser Schienennetz erstreckt sich</yellow>
                <prefix/><yellow>In alle Himmelsrichtungen.</yellow>
                <prefix/>
                <prefix/><gold><bold>Anleitung:</bold></gold>
                <prefix/><click:run_command:/bahnhof><red>/bahnhof</red></click></hover>
                <prefix/>
                <prefix/><gold><bold>Fahrziele wählen:</bold></gold>
                <prefix/><hover:show_text:'<green>Verfügbare Fahrziele auflisten</green>'><click:run_command:/fahrziele><red>/fahrziele</red></click></hover>
                <prefix/><yellow>oder</yellow>
                <prefix/><click:suggest_command:/fahrziel NAME><red>/fahrziel</red></click> <gray>\\<name></gray>
                <prefix/>
                <prefix/><yellow>Gute Fahrt!</yellow>
                
                <footer/>""");

    public static final Localization COMMAND_DESTINATIONS_HEAD = new Localization("command.destinations.head",
            """
                <header/>

                <hover:show_text:'<green>Informationen zum Schienennetz</green>'><click:run_command:/bahnhof><prefix/><yellow>Guten Tag, Reisender!</yellow>
                <prefix/><yellow>Verstehst du nur </yellow>
                <prefix/><red>/bahnhof</red><yellow>?</yellow></click></hover>
                <prefix/>
                <prefix/><gold><bold>Mögliche Fahrziele:</bold></gold>
                <prefix/>
                <prefix/><hover:show_text:'<green>/fahrziele Hauptbahnhof</green>'><click:run_command:/fahrziele Hauptbahnhof><green>» </green><yellow>Hauptbahnhöfe</yellow></click></hover>
                <prefix/><hover:show_text:'<green>/fahrziele Bahnhof</green>'><click:run_command:/fahrziele Bahnhof><green>» </green><yellow>Bahnhöfe</yellow></click></hover>
                <prefix/><hover:show_text:'<green>/fahrziele Spielerbahnhof</green>'><click:run_command:/fahrziele Spielerbahnhof><green>» </green><yellow>Spielerbahnhöfe</yellow></click></hover>
                <prefix/><hover:show_text:'<green>/fahrziele Öffentlich</green>'><click:run_command:/fahrziele Öffentlich><green>» </green><yellow>Öffentliche Ziele</yellow></click></hover>
                """);
    public static final Localization COMMAND_DESTINATIONS_LIST_INVALIDPAGE = new Localization("command.destinations.list.invalidpage", "<prefix/><red>Ungültige Seitennummer.</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_UNKOWNPAGE = new Localization("command.destinations.list.unkownpage", "<prefix/><red>Es gibt nur {pages} Seite(n).</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_EMPTY = new Localization("command.destinations.list.empty", "<prefix/><red>Es wurden keine Fahrziele zu dieser Auswahl gefunden.</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_INDICATOR = new Localization("command.destinations.list.indicator", " <green>{actual} <green>/<green> <green>{total}</green> ");
    public static final Localization COMMAND_DESTINATIONS_LIST_CAPTION = new Localization("command.destinations.list.caption", "<prefix/><gray> # </gray><gold><bold>{server}</bold></gold>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_PLAYER = new Localization("command.destinations.list.entry.player", "<yellow>{destination}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_OTHER = new Localization("command.destinations.list.entry.other", "<gold>{destination}</gold>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_CAPTION = new Localization("command.destinations.list.entry.hover.caption", "<green>{command}</green>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_TYPE = new Localization("command.destinations.list.entry.hover.type", "<gold>Stations-Typ: </gold><yellow>{type}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNER = new Localization("command.destinations.list.entry.hover.owner", "<gold>Besitzer: </gold><yellow>{owner}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNERUNKOWN = new Localization("command.destinations.list.entry.hover.ownerunkown", "Unbekannt");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_LOCATION = new Localization("command.destinations.list.entry.hover.location", "<gold>Koordinaten: </gold><yellow>{location}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_WORLD = new Localization("command.destinations.list.entry.hover.world", "<gold>Welt: </gold><yellow>{world}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_BTN_FORWARDS_ON = new Localization("command.destinations.btn.forwards.on", "<hover:show_text:'<green>Nächste Seite</green>'><click:run_command:{command}><gold>Weiter</gold> <green>>>----</green></click></hover>");
    public static final Localization COMMAND_DESTINATIONS_BTN_FORWARDS_OFF = new Localization("command.destinations.btn.forwards.off", "<gray>Weiter</gray> <green>>>----</green>");
    public static final Localization COMMAND_DESTINATIONS_BTN_BACKWARDS_ON = new Localization("command.destinations.btn.backwards.on", "<hover:show_text:'<green>Vorherige Seite</green>'><click:run_command:{command}><green>----<<</green> <gold>Zurück</gold></click></hover>");
    public static final Localization COMMAND_DESTINATIONS_BTN_BACKWARDS_OFF = new Localization("command.destinations.btn.backwards.off", "<green>----<<</green> <gray>Zurück</gray>");
    public static final Localization COMMAND_DESTINATIONS_BTN_TELEPORT = new Localization("command.destinations.btn.teleport", "<hover:show_text:'<green>Zum Bahnhof teleportieren</green>'><click:run_command:{command}> <gray>[</gray><white>TP</white><gray>]</gray></click></hover>");

    public static final Localization COMMAND_DESTEDIT_INFO = new Localization("command.destedit.info", """
            <header/>
            
            <prefix/><gold>Fahrziel: </gold><yellow>{name}</yellow>
            <prefix/><gold>ID: </gold><yellow>{id}</yellow>
            <prefix/><gold>Typ: </gold><yellow>{type}</yellow>
            <prefix/><gold>Besitzer: </gold><yellow>{owner}</yellow>
            <prefix/><gold>Mitwirkende: </gold><yellow>{participants}</yellow>
            <prefix/><gold>Server: </gold><yellow>{server}</yellow>
            <prefix/><gold>Welt: </gold><yellow>{world}</yellow>
            <prefix/><gold>Koordinaten: </gold><yellow>{x} {x} {z}</yellow>
            
            <footer/>
            """);
    public static final Localization COMMAND_DESTEDIT_NONAME = new Localization("command.destedit.noname", "<prefix/><red>Bitte gebe den Namen des Ziel an.</red>");
    public static final Localization COMMAND_DESTEDIT_MULTIPLEDEST = new Localization("command.destedit.multipledest", "<prefix/><red>Es existieren mehrere Ziele mit diesem Namen.<newLine><prefix/>Bitte gebe zusätzlich den Servernamen an.</red>");

    public static final Localization COMMAND_MOBENTER_SUCCESS = new Localization("command.mobenter.success", "<prefix/><green>Es wurden <yellow>{amount}</yellow> Tiere in deinen Zug gesetzt</green>");
    public static final Localization COMMAND_MOBENTER_FAILED = new Localization("command.mobenter.failed", "<prefix/><red>Es wurden keine Tiere im Umkreis von <yellow>{radius}</yellow> Blöcken gefunden oder der Zug ist leider schon voll.</red>");
    public static final Localization COMMAND_MOBEJECT_SUCCESS = new Localization("command.mobeject.success", "<prefix/><green>Es wurden alle vorhandenen Tiere aus dem Zug geworden.</green>");

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public String get() {
        return CraftBahnPlugin.plugin.getLocalizationManager().getLocale(this.getName());
    }
}
