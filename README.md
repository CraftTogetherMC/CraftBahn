# CraftBahn
   
![](https://media.tenor.com/images/b31da936191fcccadb8fc6e0fc777070/tenor.gif)

CraftBahn is a plugin for minecraft servers using Bukkit, SpigotMC or PaperMC, that we developed for our [CraftTogetherMC](https://github.com/CraftTogetherMC) server.
It serves as an add-on for the [TrainCarts](https://github.com/bergerhealer/TrainCarts) plugin and primarily serves the purpose of managing destinations.
It also adds action signs for TrainCarts which serve as cross-server portals, driving players and mobs from one server to another.

[![](https://i.imgur.com/SzkHTE8.png)](https://www.youtube.com/watch?v=8XCvmY8EPtk)  
![](https://i.imgur.com/0ngfmUA.png)

### More functions:
- Commands to get mobs on/off train(s).
- Speedometer in the player's action bar for moving trains.
- Dynmap integration (A marker is created on the map for each destination)
- All texts can be customized (localization.yml)
   
   
![](https://i.imgur.com/G2U1pKx.png)  
![](https://i.imgur.com/cUXQjis.png)  
![](https://i.imgur.com/g2UdOvJ.png)   


### Befehle & Berechtigungen:
   
#### Fahrziel wählen
| Befehl                      | Permission                            | Beschreibung                                                                             |
|:----------------------------|:--------------------------------------|:-----------------------------------------------------------------------------------------|
| `/fahrziel`                 | craftbahn.command.destination         | Zeigt Grundlegende Informationen zur Benutzung des Befehls                               |
| `/fahrziel <name>`          | craftbahn.command.destination         | Setzt dem aktuell ausgewählten Zug das angegebene Ziel                                   |
| `/fahrziele [typ]`          | craftbahn.command.destinations        | Zeigt eine Liste mit allen Fahrzielen                                                    |
| `/fahrziele [typ] [filter]` | craftbahn.command.destinations.filter | Zeigt eine gefilterte Liste mit allen Fahrzielen **Filter-Flags:** `--server` `--player` |
   
   
#### Fahrziele verwalten
| Befehl                                                       | Permission                              | Beschreibung                                                            |
|:-------------------------------------------------------------|:----------------------------------------|:------------------------------------------------------------------------|
| `/fahrzieledit info <destination> [server]`                  | craftbahn.command.destedit.info         | Zeigt detaillierte Informationen zum angegebenen Fahrziel an            |
| `/fahrzieledit tp <destination> [server]`                    | craftbahn.command.destedit.teleport     | Teleportiert den Spieler zur hinterlegten Position angegebenen Fahrziel |
| `/fahrzieledit add <destination> <type>  `                   | craftbahn.command.destedit.add          | Fügt ein neues Fahrziel mit dem angegebenen Stationstyp hinzu           |
| `/fahrzieledit remove <destination>  [server] `              | craftbahn.command.destedit.remove       | Entfernt ein vorhandenes Fahrziel                                       |
| `/fahrzieledit addmember <destination> <player> [server]`    | craftbahn.command.destedit.addmember    | Fügt dem angegebenen Fahrziel einen sekundären Besitzer hinzu           |
| `/fahrzieledit removemember <destination> <player> [server]` | craftbahn.command.destedit.removemember | Entfernt einen sekundären Besitzer des angegebenen Fahrziel             |
   
   
#### Sonstige Befehle
| Befehl                | Permission                 | Beschreibung                                                       |
|:----------------------|:---------------------------|:-------------------------------------------------------------------|
| `/mobenter [radius]`  | craftbahn.command.mobenter | Lässt Tiere im Umkreis des ausgewählten Zug, in den Zug einsteigen |
| `/mobeject`           | craftbahn.command.mobeject | Wirft alle Tiere aus dem ausgewählten Zug heraus                   |
   
   
### Tabellestruktur:

``` sql
CREATE TABLE `cb_destinations` (
  `id` int(11) NOT NULL,
  `name` varchar(24) NOT NULL,
  `type` varchar(24) NOT NULL,
  `server` varchar(24) NOT NULL,
  `world` varchar(24) NOT NULL,
  `loc_x` double NOT NULL,
  `loc_y` double NOT NULL,
  `loc_z` double NOT NULL,
  `owner` varchar(36) NOT NULL,
  `participants` longtext DEFAULT NULL,
  `public` tinyint(1) NOT NULL,
  `tp_x` double DEFAULT NULL,
  `tp_y` double DEFAULT NULL,
  `tp_z` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `cb_destinations`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `cb_destinations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

CREATE TABLE `cb_portals` (
  `id` int(11) NOT NULL,
  `name` int(16) NOT NULL,
  `target_host` int(255) DEFAULT NULL,
  `target_port` int(11) DEFAULT NULL,
  `target_server` int(24) DEFAULT NULL,
  `target_world` varchar(24) DEFAULT NULL,
  `target_x` double DEFAULT NULL,
  `target_y` double DEFAULT NULL,
  `target_z` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `cb_portals`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

ALTER TABLE `cb_portals`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

COMMIT;
```


