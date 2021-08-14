-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Erstellungszeit: 14. Aug 2021 um 16:29
-- Server-Version: 10.3.31-MariaDB-0ubuntu0.20.04.1
-- PHP-Version: 7.4.21

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `craftbahn`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `cb_portals`
--

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

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `cb_portals`
--
ALTER TABLE `cb_portals`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `cb_portals`
--
ALTER TABLE `cb_portals`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
