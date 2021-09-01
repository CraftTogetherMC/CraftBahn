package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.CTLocation;
import de.crafttogether.craftbahn.util.Message;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;

public class SignActionPortalOut extends SignAction {

    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("portal-out");
    }

    @Override
    public void execute(SignActionEvent event) { }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] lines = event.getLines();

        // Validate third line
        if (lines[2].length() < 1) {
            event.getPlayer().sendMessage("&§Please write a name for this portal on the third line");
            displayError(event);
            return false;
        }

        // Get portal from database or create new entry
        CraftBahnPlugin.getInstance().getPortalStorage().getOrCreate(lines[2], (err1, portal) -> {
            if (err1 != null) {
                Message.debug(event.getPlayer(), err1.getMessage());
                err1.printStackTrace();
            }

            portal.setTargetHost("127.0.0.1");
            portal.setTargetPort(CraftBahnPlugin.getInstance().getConfig().getInt("Settings.Port"));
            portal.setTargetLocation(CTLocation.fromBukkitLocation(event.getLocation()));

            CraftBahnPlugin.getInstance().getPortalStorage().update(portal, (err2, updated) -> {
                if (err2 != null) {
                    Message.debug(event.getPlayer(), err2.getMessage());
                    err2.printStackTrace();
                }
            });
        });

        // Respond
        return SignBuildOptions.create()
            .setName("Portal-Exit").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
            .handle(event.getPlayer());
    }

    private void displayError(SignActionEvent event) {
        // When not successful, display particles at the sign to indicate such
        BlockFace facingInv = event.getFacing().getOppositeFace();
        Location effectLocation = event.getRailLocation()
                .add(0.5, 0.5, 0.5)
                .add(0.3 * facingInv.getModX(), 0.0, 0.3 * facingInv.getModZ());

        Util.spawnParticle(effectLocation, Particle.BARRIER);
        WorldUtil.playSound(effectLocation, SoundEffect.EXTINGUISH, 1.0f, 2.0f);
    }
}
