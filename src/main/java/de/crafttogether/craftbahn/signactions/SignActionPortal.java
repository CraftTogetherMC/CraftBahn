package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.portals.Portal;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.sql.SQLException;
import java.util.List;

public class SignActionPortal extends SignAction {
    private CraftBahnPlugin plugin = CraftBahnPlugin.plugin;

    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("portal");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (!event.isPowered()) return;

        // Train arrives sign
        if (event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasGroup()) {
            Util.debug("#trainEnter");

        }

        // Cart arrives sign
        if (event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasMember()) {
            Util.debug("#cartEnter");
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] lines = event.getLines();
        String portalName = lines[2];

        // Validate third line
        if (LogicUtil.nullOrEmpty(portalName)) {
            Localization.PORTAL_CREATESIGN_FAILED_NAME.message(event.getPlayer());
            displayError(event);
            return false;
        }

        // Get existing portals from database
        List<Portal> portals = null;
        try {
            portals = plugin.getPortalStorage().get(portalName, Portal.PortalType.BIDIRECTIONAL);
        } catch (SQLException e) {
            Localization.COMMAND_ERROR.message(event.getPlayer(),
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            return false;
        }

        // Create sign
        if (portals.size() == 0 || portals.size() == 1) {

            // First sign created
            if (portals.size() == 0) {
                Localization.PORTAL_CREATESIGN_FIRST.message(event.getPlayer(),
                        PlaceholderResolver.resolver("name", portalName));
            }

            // Sign updated
            else if (portals.get(0).getTargetLocation().getBukkitLocation().equals(event.getLocation())) {
                plugin.getLogger().info("SIGN UPDATED");
                return true;
            }

            // Second sign created
            else {
                Portal portal = portals.get(0);

                // One sign on this server already exists
                if (portal.getTargetLocation().getServer().equals(plugin.getServerName())) {
                    Localization.PORTAL_CREATESIGN_FAILED_SAMESERVER.message(event.getPlayer());
                    return false;
                }

                Localization.PORTAL_CREATESIGN_SECOND.message(event.getPlayer(),
                        PlaceholderResolver.resolver("name", portal.getName()),
                        PlaceholderResolver.resolver("server", portal.getTargetLocation().getServer()),
                        PlaceholderResolver.resolver("world", portal.getTargetLocation().getWorld()),
                        PlaceholderResolver.resolver("x", String.valueOf(portal.getTargetLocation().getX())),
                        PlaceholderResolver.resolver("y", String.valueOf(portal.getTargetLocation().getY())),
                        PlaceholderResolver.resolver("z", String.valueOf(portal.getTargetLocation().getZ())));
            }

            // Save to database
            try {
                Portal portal = plugin.getPortalStorage().create(
                        portalName,
                        Portal.PortalType.BIDIRECTIONAL,
                        plugin.getConfig().getString("Portals.Host"),
                        plugin.getConfig().getInt("Portals.Port"),
                        CTLocation.fromBukkitLocation(event.getLocation()));
            } catch (SQLException e) {
                Localization.COMMAND_ERROR.message(event.getPlayer(),
                        PlaceholderResolver.resolver("error", e.getMessage()));

                e.printStackTrace();
                return false;
            }
        }

        // There are already two signs
        else {
            Localization.PORTAL_CREATESIGN_FAILED_EXISTS.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));
            return false;
        }

        SignBuildOptions.create()
                .setName("Portal-Exit").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
                .handle(event.getPlayer());

        return true;
    }

    private void displayError(SignActionEvent info) {
        BlockFace facingInv = info.getFacing().getOppositeFace();
        Location effectLocation = info.getSign().getLocation()
                .add(0.5, 0.5, 0.5)
                .add(0.3 * facingInv.getModX(), 0.0, 0.3 * facingInv.getModZ());

        com.bergerkiller.bukkit.tc.Util.spawnDustParticle(effectLocation, 255.0, 255.0, 0.0);
        WorldUtil.playSound(effectLocation, SoundEffect.EXTINGUISH, 1.0f, 2.0f);
    }
}