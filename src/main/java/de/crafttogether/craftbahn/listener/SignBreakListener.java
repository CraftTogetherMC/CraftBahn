package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.tc.SignActionHeader;
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

        SignActionHeader signActionHeader = SignActionHeader.parseFromSign(sign);
        if (!signActionHeader.isTrain() || sign.line(2).equals("portal") || sign.line(2).equals("portal-out") || sign.line(2).equals("portal-in")) return;

        Portal portal = CraftBahnPlugin.plugin.getPortalStorage().getPortal(sign.getBlock().getLocation());
        CraftBahnPlugin.plugin.getPortalStorage().delete(portal.getId(), (err, rows) -> {});
    }
}
