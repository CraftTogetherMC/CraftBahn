**CraftBahn** _(CTDestinations rewritten)_

![](https://media.tenor.com/images/b31da936191fcccadb8fc6e0fc777070/tenor.gif)

**TODO:**
- WICHTIG: ICS: Items löschen beim Port in die Creative-Welt (Doppelte Pluginsicherheit, die über Schilder ist natürlich nicht 100% sicher ^^)
- type PUBLIC_STATION Icon(Chest_Minecard) //Livemap
- `/fze settype` / `/fze updatemarker` prüfen
- Debugging (Ein Job für Ceddix)- 
- ~~`/fze <action> <destination> [server]`~ -> Destination jeweils anhand des verbundenen Server auswählen (wenn kein anderer angegeben)`~~
- ~~`/fahrziel` <destination> -> Alle gefundenen Fahrziele mit dem angegebenen Namen sortiert nach server auflisten~~
- ~~Bestehende Daten aus altem Plugin importieren (php)~~
- ~~/fahrzieledit setwarp -cmd für das setzen der Teleport-Position hinzufügen (setlocation == MarkerPosition)~~
- ~~/farhzieledit add/remove-owner -cmd für die Verwaltung weiterer Besitzer (participants) hinzufügen.~~
- ~~TrainListener wieder einfügen (Für EnterMessages und weitere CraftBahn features)~~
- ~~Inhaltsverzeichnis für `/fahrziele` mit klickbaren Links~~
- ~~Bei /fahrziel Route setzen wenn Ziel sich auf anderem Server befindet~~ 
- ~~/fahrziele (Liste) übersichtlicher gestalten.. (Mit Pagination)~~
- ~~Befehl hinzufügen um Info's über ein bestimmtes Fahrziel anzuzeigen `/fze info <name> [server]`~~
- ~~Route / Destination über TrainCartsAPI setzen anstatt jeweiliges command auszuführen. (Um Rückgabe zu vermeiden)~~
- Ausgabe im Chat was gespawned wurde (Minecart oder Traincart) wenn man ein Minecart placed
- ~~MarkerSystem überarbeiten~~
- PluginMessage: Andere server im Netzwerk bei Änderungen benachrichtigen
- ICS integrieren
- Züge mit kurzer Verzögerung löschen wenn Spieler ausgestiegen ist. (Benötigt Tag-Signs an allen Bahnhöfen!)
- ViewDistance bei der Zugfahrt für mitfahrende Spieler senken https://github.com/Spottedleaf/Tuinity/commit/1ed460a26b4266b9573d7f28202ca4022784c5d9

**Probleme:**
- Direktes anzeigen von Büchern bedarf weiterer recherche.
- Sämtliche Texte im Buch müssen mit anderen Farben dargestellt werden.

**Ideen:**
- ICS: LinkRegistry (MySQL) Für einfachere Syntax auf ActionSigns (receive & link)
- ICS: Zug nach teleport für eine kurze konfigurierbare Zeit anhalten. (Für sauberes Chunkloading)
- ICS: "Besserer" Übergang (z.B. durch Blindness-Effekt)

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
  
COMMIT;
```
