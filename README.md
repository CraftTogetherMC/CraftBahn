# CraftBahn
### Dependency: [MySQLAdapter](https://github.com/CraftTogetherMC/MySQLAdapter/releases/tag/Plugin)  
  
![](https://media.tenor.com/images/b31da936191fcccadb8fc6e0fc777070/tenor.gif)


CraftBahn is a plugin for minecraft servers using [Bukkit](https://bukkit.org), [SpigotMC](https://www.spigotmc.org) or [PaperMC](https://papermc.io), that we developed for our [CraftTogetherMC](https://github.com/CraftTogetherMC) minecraft server.
It serves as an add-on for the [TrainCarts](https://github.com/bergerhealer/TrainCarts) plugin and primarily serves the purpose of managing destinations.
It also adds action signs for TrainCarts which serve as cross-server portals, driving players and mobs from one server to another.

### A big thank you and lots of love go out to [TeamBergerhealer](https://github.com/bergerhealer)
Also a lot of appreciation goes to the People behind [Cloud](https://github.com/Incendo/cloud) and [Adventure](https://github.com/KyoriPowered/adventure)!

[![](https://i.imgur.com/SzkHTE8.png)](https://www.youtube.com/watch?v=8XCvmY8EPtk)  

### Features:
- Cross-Server Portals (Pathfinding supported!)
- Fancy paginated list off destinations (cross-server)
- Choose a destination for your train with `/destination` and get appropriate feedback.
- Dynmap integration (A marker is created on the map for each destination)
- Speedometer in the player's action bar for moving trains.
- Commands to get mobs on/off train(s).
- All texts can be customized (localization.yml) 

### Action-Signs

|                                      |                                                                                                                                                                                                                                                |
|:-------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![](https://i.imgur.com/F0sMhvF.png) | **Bidirectional Portal**<br/><br/>This sign represents a portal entrance and exit at the same time.<br/>A pair of these signs that have the same channel name can be passed from either side.<br/>The other sign acts as a portal exit.        |
| ![](https://i.imgur.com/ybuisvC.png) | **Directional Portal** *(Entrance)*<br/><br/>This sign represents a portal entrance.<br/>A portal of this type requires a `portal-out`-sign that has the same channel name.<br/>Any number of `portal-in`-signs can be created for one channel. |
| ![](https://i.imgur.com/3UlGw1q.png) | **Directional Portal** *(Exit)*<br/><br/>This sign represents a portal exit.<br/>Signs of this type can only exist once per channel name.                                                                                                      |

### Cross-Server Pathfinding
For cross-server pathfinding support, a corresponding portal is required for each world, which has the name of the world as the channel name.

If a player chooses a target that is on another server, e.g. freebuild2, then the plugin will create a route for the associated train.
The first destination of the route is the server name or channel name and the second is the player's desired destination

### Commands & Permissions:
   
#### Select Destination
| Command                         | Permissions                           | Description                                                                       |
|:--------------------------------|:--------------------------------------|:----------------------------------------------------------------------------------|
| `/destination`                  | craftbahn.command.destination         | Shows basic information about using the command                                   |
| `/destination <name>`           | craftbahn.command.destination         | Sets the specified destination to the currently selected train                    |
| `/destinations [type]`          | craftbahn.command.destinations        | Shows a list of all destinations                                                  |
| `/destinations [type] [filter]` | craftbahn.command.destinations.filter | Shows a filtered list of all destinations **Filter flags:** `--server` `--player` |  
   
#### Manage destinations
| Command                                                   | Permissions                             | Description                                                   |
|:----------------------------------------------------------|:----------------------------------------|:--------------------------------------------------------------|
| `/destedit info <destination> [server]`                   | craftbahn.command.destedit.info         | Displays detailed information about the specified destination |
| `/destedit tp <destination> [server]`                     | craftbahn.command.destedit.teleport     | Teleports the player to the specified destination             |
| `/destinationedit add <destination> <type> `              | craftbahn.command.destedit.add          | Adds a new destination with the specified station type        |
| `/destedit remove <destination> [server] `                | craftbahn.command.destedit.remove       | Removes an existing destination                               |
| `/destedit addmember <destination> <player> [server]`     | craftbahn.command.destedit.addmember    | Adds a secondary owner to the specified destination           |
| `/destedit removemember <destination> <player> [server]`  | craftbahn.command.destedit.removemember | Removes a secondary owner of the specified destination        |
   
#### Other commands
| Befehl                | Permission                 | Beschreibung                                                |
|:----------------------|:---------------------------|:------------------------------------------------------------|
| `/mobenter [radius]`  | craftbahn.command.mobenter | Allows animals around the selected train to board the train |
| `/mobeject`           | craftbahn.command.mobeject | Ejects all animals from the selected train                  |

### Libraries used 
- [BKCommonLib](https://github.com/bergerhealer) (Extensive plugin library)
- [Cloud](https://github.com/Incendo/cloud) (Command Framework)
- [Adventure](https://github.com/KyoriPowered/adventure) (UI)
  
  
### MySQL Table-structure:

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


