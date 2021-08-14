package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class TCHelper {
    private static SignActionPortalIn signActionPortalIn;
    private static SignActionPortalOut signActionPortalOut;

    public TCHelper() {
        signActionPortalIn = new SignActionPortalIn();
        signActionPortalOut = new SignActionPortalOut();
    }

    public static void registerActionSigns() {
        SignAction.register(signActionPortalIn);
        SignAction.register(signActionPortalOut);
    }

    public static void unregisterActionSigns() {
        SignAction.unregister(signActionPortalIn);
        SignAction.unregister(signActionPortalOut);
    }

    public static MinecartGroup getTrain(Player p) {
        Entity entity = p.getVehicle();
        MinecartMember cart = null;

        if (entity == null)
            return null;

        if (entity instanceof Minecart)
            cart = MinecartMemberStore.getFromEntity((Minecart) entity);

        if (cart != null)
            return cart.getGroup();

        return null;
    }

    public static MinecartGroup getTrain(String trainName) {
        for (MinecartGroup group : MinecartGroupStore.getGroups()) {
            if (group.getProperties().getTrainName().equals(trainName))
                return group;
        }

        return null;
    }

    public static void trainMessage(MinecartGroup group, String message) {
        for (MinecartMember<?> member : group)
            cartMessage(member, message);
    }

    public static void cartMessage(MinecartMember member, String message) {
        CommonEntity vehicle = CommonEntity.get(member.getEntity().getEntity());

        for (Object passenger : vehicle.getPlayerPassengers()) {
            if (passenger instanceof Player) {
                Player player = (Player) passenger;
                player.sendMessage(message);
            }
        }
    }
}
