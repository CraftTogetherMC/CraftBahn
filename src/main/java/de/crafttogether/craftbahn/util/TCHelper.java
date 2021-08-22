package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberChest;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TCHelper {
    private static final SignActionPortalIn signActionPortalIn = new SignActionPortalIn();
    private static final SignActionPortalOut signActionPortalOut = new SignActionPortalOut();

    public static void registerActionSigns() {
        SignAction.register(signActionPortalIn);
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

    public static List<Player> getPassengers(MinecartMember<?> member) {
        List<Player> passengers = new ArrayList<>();
        CommonEntity entity = CommonEntity.get(member.getEntity().getEntity());

        if (!(member instanceof MinecartMemberRideable))
            return passengers;

        for (Object passenger : entity.getPlayerPassengers())
            passengers.add((Player) passenger);

        return passengers;
    }

    public static List<Player> getPassengers(MinecartGroup group) {
        List<Player> passengers = new ArrayList<>();

        for (MinecartMember member : group)
            passengers.addAll(getPassengers(member));

        return passengers;
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
                player.sendMessage(message);
        }
    }
}
