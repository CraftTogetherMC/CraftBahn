package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class SignActionTransform extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("transform");
    }

    @Override
    public void execute(SignActionEvent info) {
        if (!info.isPowered()) return;

        // When a [train] sign is placed, activate when powered by redstone when the train
        // goes over the sign, or when redstone is activated.
        String trainName = info.getLine(1);

        if (trainName == null || !TrainCarts.plugin.getSavedTrains().containsTrain(trainName))
            displayError(info);

        if (info.isTrainSign()
                && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)
                && info.hasGroup()
        ) {
            for (MinecartMember<?> member : info.getGroup()) {
                //sendGreetingForCart(info, member);
            }
            return;
        }

        // When a [cart] sign is placed, activate when powered by redstone when each cart
        // goes over the sign, or when redstone is activated.
        if (info.isCartSign()
                && info.isAction(SignActionType.MEMBER_ENTER, SignActionType.REDSTONE_ON)
                && info.hasMember()
        ) {
            //sendGreetingForCart(info, info.getMember());
            return;
        }
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

    @Override
    public boolean build(SignChangeActionEvent event) {
        // This is executed when a player places down a sign matching this sign action
        // Permissions are checked and then a message is displayed to the player
        // For simplicity you can use the SignBuildOptions API for this.
        // You are free to use your own code here that checks permissions/etc.
        return SignBuildOptions.create()
            .setName(event.isCartSign() ? "cart transformer" : "train transformer")
            .setDescription("transform a train into another saved train")
            .handle(event.getPlayer());
    }
}
