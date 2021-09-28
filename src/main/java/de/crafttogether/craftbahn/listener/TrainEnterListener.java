package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.tasks.Speedometer;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class TrainEnterListener implements Listener {

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        Player p = TCHelper.getPlayer(e.getEntered());
        if (p == null) return;

        MinecartMember<?> cart = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (cart == null) return;

        // Set new enterMessage
        String enterMessage = cart.getProperties().getEnterMessage();
        if (enterMessage.equalsIgnoreCase("cbDefault")) {
            cart.getProperties().setEnterMessage("");
            sendEnterMessage(p, cart);
        }

        // Add Speedometer for train if no one exists
        String trainName = cart.getGroup().getProperties().getTrainName();
        Speedometer speedometer = CraftBahnPlugin.getInstance().getSpeedometer();
        if (speedometer.get(trainName) == null)
            speedometer.add(trainName);

        /* Set View-Distance */
        //p.setNoTickViewDistance(6);
        //p.setViewDistance(6);
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        Player p = TCHelper.getPlayer(e.getExited());
        if (p == null) return;

        MinecartMember<?> cart = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (cart == null) return;

        // Check if train has no more passengers
        if (TCHelper.getPlayerPassengers(cart.getGroup()).size() <= 1) {
            // Remove Speedometer if activated
            CraftBahnPlugin.getInstance().getSpeedometer().remove(cart.getGroup().getProperties().getTrainName());

            // Destroy train if it's tagged as "onTrack" and moving
            Bukkit.getScheduler().runTaskLater(CraftBahnPlugin.getInstance(), () -> {
                boolean onTrack = cart.getGroup().getProperties().getTags().contains("onTrack");

                if (onTrack && cart.isMoving())
                    cart.getGroup().destroy();
            }, 20L*5);
        }

        // Clear ActionBar
        p.sendActionBar(Component.text(""));

        /* Set View-Distance */
        //p.setNoTickViewDistance(p.getWorld().getNoTickViewDistance());
        //p.setViewDistance(p.getWorld().getViewDistance());
    }

    private void sendEnterMessage(Player p, MinecartMember<?> cart) {
        TextComponent message = Message.format("&e-------------- &c&lCraftBahn &e--------------");
        message.addExtra(Message.newLine());
        message.addExtra(Message.newLine());
        message.addExtra(Message.format("&6CraftBahn &8» &eGuten Tag, Reisender!"));
        message.addExtra(Message.newLine());
        message.addExtra(Message.format("&6CraftBahn &8» &eVerstehst du nur "));
        message.addExtra(Message.format("&c/bahnhof&e?"));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bahnhof"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&2Informationen zum Schienennetz"))).create()));
        p.spigot().sendMessage(message);

        if (cart.getProperties().getDestination().isEmpty()) {
            message = new TextComponent(Message.format("&6CraftBahn &8»"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8» &c&lHinweis:"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8» &cDieser Zug hat noch kein Fahrziel."));

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Verfügbare Fahrziele anzeigen"))).create()));
            p.spigot().sendMessage(message);
        } else {
            message = new TextComponent(Message.format("&6CraftBahn &8» "));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8» &eDieser Zug versucht, das Ziel:"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8» &f'&6&l" + cart.getProperties().getDestination() + "&f' &ezu erreichen."));

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Anderes Fahrziel auswählen")).create())));
            p.spigot().sendMessage(message);
        }

        message = new TextComponent(Message.newLine());
        message.addExtra(Message.format("&e----------------------------------------"));
        p.spigot().sendMessage(message);
    }
}
