package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.craftbahn.util.Message;

public class SignActionPortalOut extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("portal-out");
    }

    @Override
    public void execute(SignActionEvent info) { }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] lines = event.getLines();

        if (lines[2].length() < 1) {
            event.getPlayer().sendMessage(Message.format("&c\nPlease write a name for this portal on the third line"));
            return false;
        }

        SignBuildOptions opt = SignBuildOptions.create()
            .setName("Portal-Exit")
            .setDescription("allow trains to travel between servers");

        return opt.handle(event.getPlayer());
    }
}
