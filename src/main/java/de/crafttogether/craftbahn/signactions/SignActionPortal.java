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
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.Util;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SignActionPortal extends SignAction {
    private final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;

    @Override
    public boolean match(SignActionEvent event) {
        return event.getLine(1).equalsIgnoreCase("portal");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (!event.isPowered() || !event.isTrainSign() || event.getGroup() == null)
            return;

        PortalHandler portalHandler = plugin.getPortalHandler();

        if (!portalHandler.getPendingTeleports().containsKey(event.getGroup()) && event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasGroup())
            portalHandler.handleTrain(event);

        if (portalHandler.getPendingTeleports().containsKey(event.getGroup()) && event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasMember())
            portalHandler.handleCart(event);
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        Util.debug("build");
        String[] lines = event.getLines();
        String portalName = lines[2];

        // Validate third line
        if (LogicUtil.nullOrEmpty(portalName)) {
            Localization.PORTAL_CREATE_NONAME.message(event.getPlayer());
            displayError(event);
            return false;
        }

        // Get existing portals from database
        List<Portal> portals;
        try {
            portals = plugin.getPortalStorage().get(portalName).stream()
                    .filter(portal -> portal.getType().equals(Portal.PortalType.BIDIRECTIONAL))
                    .collect(Collectors.toList());
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
                Localization.PORTAL_CREATE_BIDIRECTIONAL_INFO_FIRST.message(event.getPlayer(),
                        PlaceholderResolver.resolver("name", portalName));
            }

            // Sign updated
            else if (portals.get(0).getTargetLocation().equals(event.getLocation())) {
                // TODO: Handle sign-updates
                return true;
            }

            // Second sign created
            else {
                Portal portal = portals.get(0);

                // One sign on this server already exists
                if (portal.getTargetLocation().getServer().equals(plugin.getServerName())) {
                    Localization.PORTAL_CREATE_BIDIRECTIONAL_SAMESERVER.message(event.getPlayer());
                    return false;
                }

                Localization.PORTAL_CREATE_BIDIRECTIONAL_INFO_SECOND.message(event.getPlayer(),
                        PlaceholderResolver.resolver("name", portal.getName()),
                        PlaceholderResolver.resolver("server", portal.getTargetLocation().getServer()),
                        PlaceholderResolver.resolver("world", portal.getTargetLocation().getWorld()),
                        PlaceholderResolver.resolver("x", String.valueOf(portal.getTargetLocation().getX())),
                        PlaceholderResolver.resolver("y", String.valueOf(portal.getTargetLocation().getY())),
                        PlaceholderResolver.resolver("z", String.valueOf(portal.getTargetLocation().getZ())));
            }

            // Save to database
            try {
                plugin.getPortalStorage().create(
                        portalName,
                        Portal.PortalType.BIDIRECTIONAL,
                        plugin.getConfig().getString("Portals.Server.PublicAddress"),
                        plugin.getConfig().getInt("Portals.Server.Port"),
                        CTLocation.fromBukkitLocation(event.getLocation()));
            } catch (SQLException e) {
                Localization.COMMAND_ERROR.message(event.getPlayer(),
                        PlaceholderResolver.resolver("error", e.getMessage()));

                e.printStackTrace();
                return false;
            }

            Localization.PORTAL_CREATE_BIDIRECTIONAL_SUCCESS.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));
        }

        // There are already two signs
        else {
            Localization.PORTAL_CREATE_BIDIRECTIONAL_EXISTS.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));
            return false;
        }

        SignBuildOptions.create()
                .setName("ServerPortal").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
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