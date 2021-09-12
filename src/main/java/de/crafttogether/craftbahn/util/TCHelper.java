package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberChest;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class TCHelper {
    private static SignActionPortalIn signActionPortalIn;
    private static SignActionPortalOut signActionPortalOut;

    public static void registerActionSigns() {
        signActionPortalIn = new SignActionPortalIn();
        SignAction.register(signActionPortalIn);

        signActionPortalOut = new SignActionPortalOut();
        SignAction.register(signActionPortalOut);
    }

    public static void unregisterActionSigns() {
        SignAction.unregister(signActionPortalIn);
        SignAction.unregister(signActionPortalOut);
    }

    // Get player from entity
    public static Player getPlayer(Entity entity) {
        if (entity instanceof Player)
            return (Player) entity;

        return null;
    }

    // Get train by player
    public static MinecartGroup getTrain(Player p) {
        Entity entity = p.getVehicle();
        MinecartMember<?> cart = null;

        if (entity == null)
            return null;

        if (entity instanceof Minecart)
            cart = MinecartMemberStore.getFromEntity(entity);

        if (cart != null)
            return cart.getGroup();

        return null;
    }

    // Get train by name
    public static MinecartGroup getTrain(String trainName) {
        for (MinecartGroup group : MinecartGroupStore.getGroups()) {
            if (group.getProperties().getTrainName().equals(trainName))
                return group;
        }

        return null;
    }

    // Clear inventory if given MinecartMember is a chest-minecart
    public static void clearInventory(MinecartMember<?> member) {
        if (member instanceof MinecartMemberChest) {
            MinecartMemberChest chestCart = (MinecartMemberChest) member;
            chestCart.getEntity().getInventory().clear();
        }
    }

    // Clear inventory of all chest-minecarts in a given MinecartGroup
    public static void clearInventory(MinecartGroup group) {
        for (MinecartMember member : group)
            clearInventory(member);
    }


    // Get all player-passengers from a train
    public static Collection<Player> getPlayerPassengers(MinecartGroup group) {
        Collection<Player> passengers = new ArrayList<>();

        for (MinecartMember<?> member : group)
            passengers.addAll(member.getEntity().getPlayerPassengers());

        return passengers;
    }

    // Get all player-passengers from a cart
    public static Collection<Player> getPlayerPassengers(MinecartMember<?> member) {
        return member.getEntity().getPlayerPassengers();
    }

    // Send message to all passengers of a train
    public static void sendMessage(MinecartGroup group, String message) {
        for (MinecartMember<?> member : group)
            sendMessage(member, message);
    }

    // Send message to all passengers of a cart
    public static void sendMessage(MinecartMember<?> member, String message) {
        CommonEntity<?> vehicle = CommonEntity.get(member.getEntity().getEntity());

        for (Object passenger : vehicle.getPlayerPassengers()) {
            if (passenger instanceof Player player)
                player.sendMessage(Component.text(message));
        }
    }

    // Send actionbar to all passengers of a train
    public static void sendActionbar(MinecartGroup group, String message) {
        for (MinecartMember<?> member : group)
            sendActionbar(member, message);
    }

    // Send permission-based actionbar to all passengers of a train
    public static void sendActionbar(MinecartGroup group, String permission, String message) {
        for (MinecartMember<?> member : group)
            sendActionbar(member, permission, message);
    }

    // Send actionBar to all passengers of a cart
    public static void sendActionbar(MinecartMember<?> member, String message) {
        CommonEntity<?> vehicle = CommonEntity.get(member.getEntity().getEntity());

        for (Object passenger : vehicle.getPlayerPassengers()) {
            if (passenger instanceof Player player)
                player.sendActionBar(Component.text(message));
        }
    }

    // Send permission-based actionbar to all passengers of a cart
    public static void sendActionbar(MinecartMember<?> member, String permission, String message) {
        CommonEntity<?> vehicle = CommonEntity.get(member.getEntity().getEntity());

        for (Object passenger : vehicle.getPlayerPassengers()) {
            if (passenger instanceof Player player && player.hasPermission(permission))
                player.sendActionBar(Component.text(message));
        }
    }

    // Send debug-message to all passengers of a train
    public static void sendDebugMessage(MinecartGroup group, String message) {
        for (MinecartMember<?> member : group)
            sendDebugMessage(member, message);
    }

    // Send debug-message to all passengers of a cart
    public static void sendDebugMessage(MinecartMember member, String message) {
        CommonEntity<?> vehicle = CommonEntity.get(member.getEntity().getEntity());

        for (Object passenger : vehicle.getPlayerPassengers()) {
            if (passenger instanceof Player player)
                Message.debug(player, message);
        }
    }
}
