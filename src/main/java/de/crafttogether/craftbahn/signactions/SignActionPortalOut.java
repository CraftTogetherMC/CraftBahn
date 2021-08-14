package de.crafttogether.craftbahn.signactions;

package de.crafttogether.tcdebug;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import org.bukkit.entity.Entity;

import java.util.List;

public class SignActionPortalOut extends SignAction {

    /**
     * Checks whether a sign action event is meant for this type of Sign Action
     *
     * @param info event
     * @return True if it matched, False if not
     */
    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("portal-out");
    }

    /**
     * Fired when this sign is being executed for a certain event
     *
     * @param info event
     */
    @Override
    public void execute(SignActionEvent info) {
        if (info.isAction(SignActionType.MEMBER_ENTER) && info.isPowered() && info.hasMember()) {
            MinecartGroup group = info.getGroup();
            MinecartMember cart = info.getMember();


            List<Entity> passengers1 = info.getMember().getEntity().getPassengers();

            List<Entity> passengers2 = info.getMember().getEntity().getEntity().getPassengers();

            CommonEntity commonEntity = CommonEntity.get(info.getMember().getEntity().getEntity());
            List<Entity> passengers3 = commonEntity.getPlayerPassengers();


            System.out.println("TCDebug: " + passengers1.size());
            System.out.println("TCDebug: " + passengers2.size());
            System.out.println("TCDebug: " + passengers3.size());

            // Destroy cart/group
            if (group.size() <= 1)
                group.destroy();
            else
                cart.onDie(true);
        }
    }

    /**
     * Fired when a sign is being built
     *
     * @param event containing relevant Build information
     * @return True if building is allowed, False if not
     */
    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] info = event.getLines();
        String[] coords = info[3].split(" ");

        try {
            Double.parseDouble(coords[0]);
            Double.parseDouble(coords[1]);
            Double.parseDouble(coords[2]);
        } catch (Exception ex) {
            return false;
        }

        SignBuildOptions opt = SignBuildOptions.create()
                .setName("interlink outbound")
                .setDescription("allow trains to \"teleport\" between servers");

        return opt.handle(event.getPlayer());
    }
}
