package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.craftbahn.util.Message;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class SignActionPortalOut extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("portal-out");
    }

    @Override
    public void execute(SignActionEvent info) { }

    @Override
    public boolean build(SignChangeActionEvent info) {
        String[] lines = info.getLines();

        // Validate third line
        if (lines[2].length() < 1) {
            info.getPlayer().sendMessage(Message.format("&c\nPlease write a name for this portal on the third line"));
            displayError(info);
            return false;
        }

        // Respond
        return SignBuildOptions.create()
            .setName("Portal-Exit")
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
}
