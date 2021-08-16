package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.portals.Portal;
import de.crafttogether.craftbahn.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.sql.SQLException;

public class SignActionPortalIn extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("portal-in");
    }

    @Override
    public void execute(SignActionEvent info) {
        // Train arrives sign
        if (info.isAction(SignActionType.GROUP_ENTER) && info.isPowered() && info.hasGroup())
            onTrainEnter(info);

        // Cart arrives Sign
        if (info.isAction(SignActionType.MEMBER_ENTER) && info.isPowered() && info.hasMember())
            onCartEnter(info);
    }

    @Override
    public boolean build(SignChangeActionEvent info) {
        String[] lines = info.getLines();

        // Validate third line
        if (lines[2].length() < 1) {
            info.getPlayer().sendMessage("&§cPlease write a name for this portal on the third line");
            displayError(info);
            return false;
        }

        // Get portal from database or create new entry
        CraftBahn.getInstance().getPortalStorage().getOrCreate(lines[2], (err, portal) -> {
            if (err != null) {
                Message.debug(info.getPlayer(), err.getMessage());
                err.printStackTrace();
                return;
            }

            Bukkit.getScheduler().runTaskLaterAsynchronously(CraftBahn.getInstance(), () -> {
                if (portal.getTargetHost() == null || portal.getTargetPort() == null || portal.getTargetLocation() == null)
                    info.getPlayer().sendMessage("§cCouldn't find an §rPortal-Exit §cfor §r'§e" + lines[2] + "§r'§c! Please create one");
                else {
                    info.getPlayer().sendMessage("Portal-Exit §ewas found at §r" + portal.getTargetLocation().getServer() +
                        " (" + portal.getTargetLocation().getWorld() +
                        ", " + portal.getTargetLocation().getX() +
                        ", " + portal.getTargetLocation().getY() +
                        ", " + portal.getTargetLocation().getZ() + ")");
                }
            }, 10L);
        });

        // Respond
        return SignBuildOptions.create()
            .setName("Portal-Entrance")
            .setDescription("allow trains to travel between servers")
            .handle(info.getPlayer());
    }

    private void displayError(SignActionEvent info) {
        // When not successful, display particles at the sign to indicate such
        BlockFace facingInv = info.getFacing().getOppositeFace();
        Location effectLocation = info.getSign().getLocation()
            .add(0.5, 0.5, 0.5)
            .add(0.3 * facingInv.getModX(), 0.0, 0.3 * facingInv.getModZ());

        Util.spawnDustParticle(effectLocation, 255.0, 255.0, 0.0);
        WorldUtil.playSound(effectLocation, SoundEffect.EXTINGUISH, 1.0f, 2.0f);
    }

    private void onCartEnter(SignActionEvent info) {

    }

    private void onTrainEnter(SignActionEvent info) {

    }
}
