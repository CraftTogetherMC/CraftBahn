package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberChest;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.signactions.SignActionPortal;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TCHelper {
    private static SignActionPortal signActionPortal;
    private static SignActionPortalIn signActionPortalIn;
    private static SignActionPortalOut signActionPortalOut;

    public static void registerActionSigns() {
        signActionPortal = new SignActionPortal();
        signActionPortalIn = new SignActionPortalIn();
        signActionPortalOut = new SignActionPortalOut();

        SignAction.register(signActionPortal);
        SignAction.register(signActionPortalIn);
        SignAction.register(signActionPortalOut);
    }

    public static void unregisterActionSigns() {
        SignAction.unregister(signActionPortal);
        SignAction.unregister(signActionPortalIn);
        SignAction.unregister(signActionPortalOut);
    }

    public static MinecartGroup getTrain(Player p) {
        Entity entity = p.getVehicle();
        MinecartMember<?> member = null;

        if (entity == null)
            return null;

        if (entity instanceof Minecart)
            member = MinecartMemberStore.getFromEntity(entity);

        if (member != null)
            return member.getGroup();

        return null;
    }

    public static MinecartGroup getTrain(String trainName) {
        TrainProperties trainProperties = TrainPropertiesStore.get(trainName);
        return (trainProperties == null) ? null : trainProperties.getHolder();
    }

    public static List<Player> getPlayerPassengers(MinecartMember<?> member) {
        List<Player> passengers = new ArrayList<>();
        for (Entity passenger : member.getEntity().getEntity().getPassengers())
            if (passenger instanceof Player) passengers.add((Player) passenger);

        return passengers;
    }

    public static List<Player> getPlayerPassengers(MinecartGroup group) {
        List<Player> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPlayerPassengers(member));

        return passengers;
    }

    public static List<Entity> getPassengers(MinecartMember<?> member) {
        return member.getEntity().getPassengers();
    }

    public static List<Entity> getPassengers(MinecartGroup group) {
        List<Entity> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPassengers(member));

        return passengers;
    }

    public static boolean hasTagIgnoreCase(String tag, Collection<String> tags) {
        for (String found : tags)
            if (found.equalsIgnoreCase(tag)) return true;

        return false;
    }

    public static void clearInventory(MinecartMember<?> member) {
        if (member instanceof MinecartMemberChest) {
            MinecartMemberChest chestCart = (MinecartMemberChest) member;
            chestCart.getEntity().getInventory().clear();
        }
    }

    public static void clearInventory(MinecartGroup group) {
        for (MinecartMember member : group)
            clearInventory(member);
    }

    public static void sendMessage(MinecartMember member, Localization localization, PlaceholderResolver... arguments) {
        for (Player passenger : getPlayerPassengers(member))
            localization.message(passenger, arguments);
    }

    public static void sendMessage(MinecartGroup group, Localization localization, PlaceholderResolver... arguments) {
        for (Player passenger : getPlayerPassengers(group))
            localization.message(passenger, arguments);
    }

    public static void sendMessage(MinecartMember member, Component message) {
        for (Player passenger : getPlayerPassengers(member))
            passenger.sendMessage(message);
    }

    public static void sendMessage(MinecartGroup group, Component message) {
        for (Player passenger : getPlayerPassengers(group))
            passenger.sendMessage(message);
    }
}
