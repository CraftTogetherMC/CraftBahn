package de.crafttogether.craftbahn.signactions;

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

    private void onCartEnter(SignActionEvent info) {

    }

    private void onTrainEnter(SignActionEvent info) {

    }
}
