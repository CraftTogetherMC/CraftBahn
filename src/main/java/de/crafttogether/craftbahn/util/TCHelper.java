package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class TCHelper {
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
}
