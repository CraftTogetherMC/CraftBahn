package de.crafttogether.craftbahn.signactions;

import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionMobCategory;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.portals.Portal;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignActionPortalIn extends SignAction {
    private Map<MinecartGroup, Portal> pendingTeleports = new HashMap<>();

    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("portal-in");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (!event.isPowered()) return;

        // Train arrives sign
        if (!pendingTeleports.containsKey(event.getGroup()) && event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasGroup())
            onTrainEnter(event);

        // Cart arrives sign
        if (pendingTeleports.containsKey(event.getGroup()) && event.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && event.hasMember())
            onCartEnter(event);
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        String[] lines = event.getLines();

        // Validate third line
        if (lines[2].length() < 1) {
            event.getPlayer().sendMessage("&§cPlease write a name for this portal on the third line");
            displayError(event);
            return false;
        }

        // Get portal from database or create new entry
        CraftBahnPlugin.getInstance().getPortalStorage().getOrCreate(lines[2], (err, portal) -> {
            if (err != null) {
                Message.debug(event.getPlayer(), err.getMessage());
                err.printStackTrace();
                return;
            }

            Bukkit.getScheduler().runTaskLaterAsynchronously(CraftBahnPlugin.getInstance(), () -> {
                if (portal.getTargetHost() == null || portal.getTargetPort() == null || portal.getTargetLocation() == null)
                    event.getPlayer().sendMessage("§cCouldn't find an §rPortal-Exit §cfor §r'§e" + lines[2] + "§r'§c! Please create one");
                else {
                    event.getPlayer().sendMessage("Portal-Exit §ewas found at §r" + portal.getTargetLocation().getServer() +
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
            .handle(event.getPlayer());
    }

    private void displayError(SignActionEvent event) {
        // When not successful, display particles at the sign to indicate such
        BlockFace facingInv = event.getFacing().getOppositeFace();
        Location effectLocation = event.getSign().getLocation()
            .add(0.5, 0.5, 0.5)
            .add(0.3 * facingInv.getModX(), 0.0, 0.3 * facingInv.getModZ());

        Util.spawnDustParticle(effectLocation, 255.0, 255.0, 0.0);
        WorldUtil.playSound(effectLocation, SoundEffect.EXTINGUISH, 1.0f, 2.0f);
    }

    private void onTrainEnter(SignActionEvent event) {
        MinecartGroup group = event.getGroup();
        String portalName = event.getLine(2);
        Portal portal = CraftBahnPlugin.getInstance().getPortalStorage().getPortal(portalName);

        if (portal == null) {
            TCHelper.sendMessage(event.getMember(), "§cCouldn't find an §rPortal-Exit §cfor §r'§e" + portalName + "§r'§c!");
            return;
        }

        // Clear Inventory if needed
        if (event.getLine(3).equalsIgnoreCase("clear"))
            TCHelper.clearInventory(group);

        // cache teleportation-infos
        pendingTeleports.put(group, portal);

        // Transmit collected Data to other server
        PortalHandler.transmitTrain(group, portal);

        // Disable collisions after train is sent to avoid they're being pushed back by players
        group.getProperties().setCollisionMode("collision", "false");
    }

    private void onCartEnter(SignActionEvent event) {
        MinecartGroup group = event.getGroup();
        Portal portal = pendingTeleports.get(group);

        if (portal == null)
            return;

        MinecartMember member = event.getMember();
        List<Player> passengers = TCHelper.getPlayerPassengers(member);

        for (Player playerPassenger : passengers)
            PortalHandler.sendToServer(playerPassenger, portal.getTargetLocation().getServer());

        // Destroy cart and remove group
        if (group.size() <= 1) {
            pendingTeleports.remove(group);
            group.destroy();
        }
        else
            member.onDie(true);
    }
}
