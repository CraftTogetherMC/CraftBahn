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
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.sql.SQLException;
import java.util.List;

public class SignActionPortalIn extends SignAction {
    private CraftBahnPlugin plugin = CraftBahnPlugin.plugin;

    @Override
    public boolean match(SignActionEvent event) {
        return event.getLine(1).equalsIgnoreCase("portal-in");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (!event.isPowered() || !event.isTrainSign())
            return;

        if (event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasGroup())
            PortalHandler.handleTrain(event);

        if (event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasMember())
            PortalHandler.handleCart(event);
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] lines = event.getLines();
        String portalName = lines[2];

        // Validate third line
        if (LogicUtil.nullOrEmpty(portalName)) {
            Localization.PORTAL_CREATE_NONAME.message(event.getPlayer());
            displayError(event);
            return false;
        }

        // Get existing portal-out -signs from database
        List<Portal> portals;
        try {
            portals = plugin.getPortalStorage().get(portalName, Portal.PortalType.OUT);
        } catch (SQLException e) {
            Localization.COMMAND_ERROR.message(event.getPlayer(),
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            return false;
        }

        if (portals.size() < 1)
            Localization.PORTAL_CREATE_IN_NOTEXIST.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));

        // Save to database
        try {
            plugin.getPortalStorage().create(
                    portalName,
                    Portal.PortalType.IN,
                    plugin.getConfig().getString("Portals.Host"),
                    plugin.getConfig().getInt("Portals.Port"),
                    CTLocation.fromBukkitLocation(event.getLocation()));
        } catch (SQLException e) {
            Localization.COMMAND_ERROR.message(event.getPlayer(),
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            return false;
        }

        Localization.PORTAL_CREATE_IN_SUCCESS.message(event.getPlayer(),
                PlaceholderResolver.resolver("name", portalName));

        SignBuildOptions.create()
                .setName("ServerPortal-Entrance").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
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