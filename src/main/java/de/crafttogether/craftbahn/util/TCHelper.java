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

    // Send message to all passengers of a train
    public static void trainMessage(MinecartGroup group, String message) {
        for (MinecartMember<?> member : group)
            cartMessage(member, message);
    }

    // Send message to all passengers of a cart
    public static void cartMessage(MinecartMember<?> member, String message) {
        CommonEntity<?> vehicle = CommonEntity.get(member.getEntity().getEntity());

        for (Object passenger : vehicle.getPlayerPassengers()) {
            if (passenger instanceof Player player)
                player.sendMessage(message);
        }
    }
}
