**CraftBahn** _(CTDestinations rewritten)_

![](https://media.tenor.com/images/b31da936191fcccadb8fc6e0fc777070/tenor.gif)

**TODO:**
- Debugging (Ein Job für Ceddix)
- ~~Bestehende Daten aus altem Plugin importieren (php)~~
- ~~/fahrzieledit setwarp -cmd für das setzen der Teleport-Position hinzufügen (setlocation == MarkerPosition)~~
- ~~/farhzieledit add/remove-owner -cmd für die Verwaltung weiterer Besitzer (participants) hinzufügen.~~
- ~~TrainListener wieder einfügen (Für EnterMessages und weitere CraftBahn features)~~
- PluginMessage: Andere server im Netzwerk bei Änderungen benachrichtigen
- Bei /fahrziel Route setzen wenn Ziel sich auf anderem Server befindet
- ~~/fahrziele (Liste) übersichtlicher gestalten.. (Mit Pagination)~~
- MarkerSystem überarbeiten

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
