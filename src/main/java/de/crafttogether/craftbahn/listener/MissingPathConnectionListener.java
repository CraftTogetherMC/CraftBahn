package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.MissingPathConnectionEvent;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MissingPathConnectionListener implements Listener {

    @EventHandler
    public void onMissingPathConnection(MissingPathConnectionEvent e) {
        Location loc = e.getPathNode().location.getLocation();
        MinecartGroup train = e.getGroup();
        String trainName = train.getProperties().getTrainName();

        String passengers = "";
        for (Player p : TCHelper.getPlayerPassengers(train))
            passengers += p.getName() + ", ";
        passengers = passengers.substring(0, passengers.length() -2);

        ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("craftbahn.debug")) return;
            Message.debug(p, Message.parse("&cEs wurde keine Verbindung zu &e" + e.getDestination() + " &cgefunden!").clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Zugname: &e" + trainName).clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Passagiere: &e" + passengers).clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Position: &e" + loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getY() + ", " + loc.getZ()).clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Der Zug kam von: &e" + train.head().getDirection().getOppositeFace().name()).clickEvent(tpEvent));
        }
    }
}
