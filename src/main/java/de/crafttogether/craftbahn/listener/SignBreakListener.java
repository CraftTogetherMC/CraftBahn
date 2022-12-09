package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.portals.Portal;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignBreakListener implements Listener {

    @EventHandler
    public void onSignAction(BlockBreakEvent event) {
        Sign sign = BlockUtil.getSign(event.getBlock());
        if (sign == null) return;

        if (Portal.isValid(sign)) {
            Portal portal = CraftBahnPlugin.plugin.getPortalStorage().getPortal(sign.getBlock().getLocation());
            CraftBahnPlugin.plugin.getPortalStorage().delete(portal.getId(), (err, rows) -> {});
        }
    }
}
