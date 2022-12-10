package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.List;

public class PortalHandler {
    private static CraftBahnPlugin plugin = CraftBahnPlugin.plugin;

    public static void handleTrain(SignActionEvent event) {
        MinecartGroup group = event.getGroup();
        String portalName = event.getLine(2);

        // Get existing portal-out -signs from database
        List<Portal> portals;
        try {
            portals = plugin.getPortalStorage().get(portalName, Portal.PortalType.OUT);
        } catch (SQLException e) {
            TCHelper.sendMessage(group, Localization.COMMAND_ERROR,
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            return;
        }

        if (portals.size() < 1 || portals.get(0).getTargetLocation() == null) {
            TCHelper.sendMessage(group, Localization.PORTAL_ENTER_NOEXIT,
                    PlaceholderResolver.resolver("name", portalName));
            return;
        }

        // Apply blindness-effect
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1);
        List<Player> playerPassengers = TCHelper.getPlayerPassengers(group);
        for (Player passenger : playerPassengers)
            passenger.addPotionEffect(blindness);

        // Clear Inventory if needed
        if (event.getLine(3).equalsIgnoreCase("clear"))
            TCHelper.clearInventory(group);

        // cache teleportation-infos
        //pendingTeleports.put(group, portal);

        // Transmit collected Data to other server
        //transmitTrain(group, portal);

        // Disable collisions after train is sent to avoid they're being pushed back by players
        group.getProperties().setCollision(CollisionOptions.CANCEL);
    }

    public static void handleCart(SignActionEvent event) {
        MinecartGroup group = event.getGroup();

        /* // TODO: Neccessary?
        Portal portal = pendingTeleports.get(group);
        if (portal == null) {
            plugin.getLogger().warning("Portal is null");

            TCHelper.sendMessage(group, Localization.COMMAND_ERROR,
                    PlaceholderResolver.resolver("error", e.getMessage()));
            return;
        } */

        MinecartMember<?> member = event.getMember();
        for (Entity passenger : member.getEntity().getEntity().getPassengers()) {

            //if (passenger instanceof Player)
                //PortalHandler.sendPlayerToServer((Player) passenger, portal);

            //else if (passenger instanceof LivingEntity)
                //PortalHandler.sendEntityToServer((LivingEntity) passenger, portal);
        }

        // Destroy cart and remove group
        if (group.size() <= 1) {
            //pendingTeleports.remove(group);
            group.destroy();
        }
        else {
            Entity cartEntity = member.getEntity().getEntity();
            group.removeSilent(member);
            cartEntity.remove();
        }
    }

    public static void reEnterPassenger(Passenger passenger, PlayerJoinEvent e) {

    }
}
