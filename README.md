# CraftBahn

![](https://media.tenor.com/images/b31da936191fcccadb8fc6e0fc777070/tenor.gif)

CraftBahn ist ein Plugin, welches wir für unseren Server [CraftTogetherMC](https://github.com/CraftTogetherMC) entwickelt haben.  
Es dient als Add-On für das Plugin [TrainCarts](https://github.com/bergerhealer/TrainCarts) und erfüllt primär den Zweck, Fahrziele zu verwalten.

### Weitere funktionen:
- Befehle um Mobs in/aus Züge(n) ein/aussteigen zu lassen.
- Geschwindigkeitsanzeige in der Actionbar des Spielers, für fahrende Züge.
- TrainCarts-ActionSigns für serverübergreifende "Teleportation" von Zügen (In Arbeit)
- Dynmap-Integration -> Für jedes Fahrziel wird ein Marker auf der Karte erstellt.
- Alle Texte können angepasst werden (localization.yml)
  
  
![](https://i.imgur.com/G2U1pKx.png)  
![](https://i.imgur.com/cUXQjis.png)  
![](https://i.imgur.com/g2UdOvJ.png)   
  
### Befehle & Berechtigungen:
| Befehl                     | Permission                             | Beschreibung     |
| :---                       | :---                                   | :---             |
| /fahrziel                  | craftbahn.command.destination          | Zeigt Grundlegende Informationen zur Benutzung des Befehls |
| /fahrziel <name>           | craftbahn.command.destination          | Setzt dem aktuell ausgewählten Zug das angegebene Ziel |
| /fahrziele [typ]           | craftbahn.command.destinations         | Zeigt eine Liste mit allen Fahrzielen
| /fahrziele [typ] [filter]  | craftbahn.command.destinations.filter  | Zeigt eine gefilterte Liste mit allen Fahrzielen **Filter-Flags:** `--server` `--player`  |
| /mobenter [radius]         | craftbahn.command.mobenter             | Lässt Tiere im Umkreis des ausgewählten Zug, in den Zug einsteigen |
| /mobeject                  | craftbahn.command.mobeject             | Wirft alle Tiere aus dem ausgewählten Zug heraus |

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

