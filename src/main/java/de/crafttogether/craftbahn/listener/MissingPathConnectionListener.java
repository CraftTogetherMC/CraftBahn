package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.MissingPathConnectionEvent;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.util.Optional;

public class MissingPathConnectionListener implements Listener {

    @EventHandler
    public void onMissingPathConnection(MissingPathConnectionEvent e) {
        Location loc = e.getPathNode().location.getLocation();
        MinecartGroup train = e.getGroup();
        String trainName = train.getProperties().getTrainName();

        String passengers = "";
        for (Player p : TCHelper.getPlayerPassengers(train))
            passengers += p.getName() + ", ";

        if (passengers.isEmpty())
            passengers = "Keine";
        else
            passengers = passengers.substring(0, passengers.length() -2);

        ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("craftbahn.debug")) return;
            Message.debug(p, Message.parse("&cEs wurde keine Verbindung zu &e" + e.getDestination() + " &cgefunden!").clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Zugname: &e" + trainName).clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Passagiere: &e" + passengers).clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Position: &e" + loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getY() + ", " + loc.getZ()).clickEvent(tpEvent));
            Message.debug(p, Message.parse("&6Herkunft: &e" + train.head().getDirection().getOppositeFace().name()).clickEvent(tpEvent));
        }

        if (CraftBahnPlugin.getInstance().getDiscordBot() != null) {
            JDA api = CraftBahnPlugin.getInstance().getDiscordBot().getApi();
            Optional<TextChannel> channelOpt = Optional.ofNullable(api.getTextChannelById("757206318389002240"));

            MessageEmbed embed = getEmbed(train, loc, passengers);

            TextChannel channel = channelOpt.get();
            channel.sendMessage(embed).complete();
        }
    }

    private static MessageEmbed getEmbed(MinecartGroup train, Location loc, String passengers) {
        EmbedBuilder embed = (new EmbedBuilder()).setColor(Color.BLUE);

        embed.setTitle("Achtung! Zugunglück!!");
        embed.setAuthor("CraftBahn");
        embed.appendDescription("Fahrziel wurde nicht gefunden!");
        embed.setFooter("/tppos " + loc.getBlockX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());

        embed.addField("Zugname", train.getProperties().getTrainName(), true);
        embed.addField("Fahrziel", train.getProperties().getDestination(), true);
        embed.addField("Passagiere", passengers, true);
        embed.addField("Herkunft", train.head().getDirection().getOppositeFace().name(), true);
        embed.addField("Position", loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getY() + ", " + loc.getZ(), true);
        embed.addField("", loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getY() + ", " + loc.getZ(), true);

        return embed.build();
    }
}
