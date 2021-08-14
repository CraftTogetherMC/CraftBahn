package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.craftbahn.util.Message;

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
        String[] lines = event.getLines();
        String status;

        // Validate third line
        if (lines[2].length() < 1) {
            event.getPlayer().sendMessage(Message.format("&c\nPlease write a name for this portal on the third line"));
            return false;
        }

        // Check if portal-exit exists
        if (true)
            status = "\n§cCouldn't find an §rPortal-Exit §cfor §r'§e" + lines[2] + "§r'§c! Please create one";
        else
            status = "\nPortal-Exit was found";

        // Respond
        SignBuildOptions opt = SignBuildOptions.create()
            .setName("Portal-Entrance")
            .setDescription("allow trains to travel between servers" + status);

        return opt.handle(event.getPlayer());
    }

    private void onCartEnter(SignActionEvent info) {

    }

    private void onTrainEnter(SignActionEvent info) {

    }
}
