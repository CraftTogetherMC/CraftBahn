**CraftBahn** _(CTDestinations rewritten)_

![](https://media.tenor.com/images/b31da936191fcccadb8fc6e0fc777070/tenor.gif)

**TODO:**
- Debugging (Ein Job für Ceddix)
- CBPortals: Warnmeldung wenn portal-out bereits existiert
- CBPortals: Erstelle portal-in nicht wenn ziel auf gleichem server ist
- CBPortals: Reload-Befehl
- Befehl: /mobeject
- Züge mit kurzer Verzögerung löschen wenn Spieler ausgestiegen ist. (Benötigt Tag-Signs an allen Bahnhöfen!)
- Alias-System um Spielerbahnhöfe anders in der Liste anzuzeigen
- /tprelative-Befehl Beispiel: `/tpr 300 north`
- Möglichkeit Beschreibung bei Fahrzielen
- Eigenes Icon für StationType: PUBLIC_STATION (Chest_Minecard)
- `/fze settype` / `/fze updatemarker` prüfen
- ~~`/fze <action> <destination> [server]`~ -> Destination jeweils anhand des verbundenen Server auswählen (wenn kein anderer angegeben)`~~
- ~~`/fahrziel` <destination> -> Alle gefundenen Fahrziele mit dem angegebenen Namen sortiert nach server auflisten~~
- ~~/fahrzieledit setwarp -cmd für das setzen der Teleport-Position hinzufügen (setlocation == MarkerPosition)~~
- ~~/farhzieledit add/remove-owner -cmd für die Verwaltung weiterer Besitzer (participants) hinzufügen.~~
- ~~TrainListener wieder einfügen (Für EnterMessages und weitere CraftBahn features)~~
- ~~Inhaltsverzeichnis für `/fahrziele` mit klickbaren Links~~
- ~~Bei /fahrziel Route setzen wenn Ziel sich auf anderem Server befindet~~ 
- ~~/fahrziele (Liste) übersichtlicher gestalten.. (Mit Pagination)~~
- ~~Befehl hinzufügen um Info's über ein bestimmtes Fahrziel anzuzeigen `/fze info <name> [server]`~~
- ~~Route / Destination über TrainCartsAPI setzen anstatt jeweiliges command auszuführen. (Um Rückgabe zu vermeiden)~~
- ~~MarkerSystem überarbeiten~~
- ~~ICS integrieren~~
- ~~ViewDistance bei der Zugfahrt für mitfahrende Spieler senken https://github.com/Spottedleaf/Tuinity/commit/1ed460a26b4266b9573d7f28202ca4022784c5d9~~
- ~~ICS: Items löschen beim Port in die Creative-Welt~~
- ~~Bestehende Daten aus altem Plugin importieren (php)~~

**Todo / Probleme:**
- CBPortals: Beim PlayerSpawnLocationEvent gibt es Probleme beim Entity-Tracking(?) wenn der Spieler schon hier zum passenger einer Entity wird

- **Ideen:**
- Speedometer per Befehl ausschaltbar machen (Wahlweise nur für die aktuelle Fahr oder Dauerhaft)
- Allgemein: Ausgabe im Chat was gespawned wurde (Minecart oder Traincart) wenn man ein Minecart placed
- CBPortals: Zug nach teleport für eine kurze konfigurierbare Zeit anhalten. (Für sauberes Chunkloading) // Ist das noch notwendig?
- CBPortals: "Besserer" Übergang (z.B. durch Blindness-Effekt)
- PluginMessage: Andere Server im Netzwerk bei Änderungen benachrichtigen (Destinations & Portals)

**Tabelle:**
![](https://craft-together.de/~irgendsoeintyp/chrome_42JbdTaOft.png)

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


