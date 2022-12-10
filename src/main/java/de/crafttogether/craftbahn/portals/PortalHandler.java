package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import de.crafttogether.craftbahn.net.TCPClient;
import de.crafttogether.craftbahn.net.TCPServer;
import de.crafttogether.craftbahn.signactions.SignActionPortal;
import de.crafttogether.craftbahn.signactions.SignActionPortalIn;
import de.crafttogether.craftbahn.signactions.SignActionPortalOut;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortalHandler {
    private static final CraftBahnPlugin plugin = CraftBahnPlugin.plugin;
    private static final SignActionPortal signActionPortal = new SignActionPortal();
    private static final SignActionPortalIn signActionPortalIn = new SignActionPortalIn();
    private static final SignActionPortalOut signActionPortalOut = new SignActionPortalOut();

    private final TCPServer tcpServer;
    private final Map<MinecartGroup, Portal> pendingTeleports = new HashMap<>();

    public PortalHandler(String host, int port) {
        // Create Server Socket
        this.tcpServer = new TCPServer(host, port);

        // Register TrainCarts-ActionSigns
        registerActionSigns();
    }

    public void handleTrain(SignActionEvent event) {
        MinecartGroup group = event.getGroup();
        String portalName = event.getLine(2);

        // Get existing portal-out -signs from database
        List<Portal> portals;
        try {
            portals = plugin.getPortalStorage().get(portalName, Portal.PortalType.OUT);
        } catch (SQLException e) {
            TCHelper.sendMessage(group, Localization.COMMAND_ERROR,
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            return;
        }

        if (portals.size() < 1 || portals.get(0).getTargetLocation() == null) {
            TCHelper.sendMessage(group, Localization.PORTAL_ENTER_NOEXIT,
                    PlaceholderResolver.resolver("name", portalName));
            return;
        }

        // Apply blindness-effect
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1);
        List<Player> playerPassengers = TCHelper.getPlayerPassengers(group);
        for (Player passenger : playerPassengers)
            passenger.addPotionEffect(blindness);

        // Clear Inventory if needed
        if (event.getLine(3).equalsIgnoreCase("clear"))
            TCHelper.clearInventory(group);

        // Cache teleportation-infos
        pendingTeleports.put(group, portals.get(0));

        // Transfer train-properties to target server
        //transferTrain(group, portal);

        // Disable collisions after train is sent to avoid they're being pushed back by players
        group.getProperties().setCollision(CollisionOptions.CANCEL);
    }

    public void handleCart(SignActionEvent event) {
        MinecartGroup group = event.getGroup();
        Portal portal = pendingTeleports.get(group);

        // Abort if no pendingTeleport was created
        if (portal == null)
            return;

        // Transfer passengers to target server
        MinecartMember<?> member = event.getMember();
        for (Entity passenger : member.getEntity().getEntity().getPassengers()) {
            if (passenger instanceof Player)
                PortalHandler.sendPlayerToServer((Player) passenger, portal);

            else if (passenger instanceof LivingEntity)
                PortalHandler.sendEntityToServer((LivingEntity) passenger, portal);
        }

        // Destroy cart and remove group
        if (group.size() <= 1) {
            //pendingTeleports.remove(group);
            group.destroy();
        }
        else {
            Entity cartEntity = member.getEntity().getEntity();
            group.removeSilent(member);
            cartEntity.remove();
        }
    }

    // Handle joined player if he was a passenger
    public static void reEnterPassenger(Passenger passenger, PlayerSpawnLocationEvent e) {
        Player player = e.getPlayer();
        int cartIndex = passenger.getCartIndex();

        // Try to find train and set player as passenge
        MinecartGroup train = passenger.getTrain();
        if (train == null)
            return;

        MinecartMember<?> member = train.get(cartIndex);

        if (member instanceof MinecartMemberRideable) {
            if (player.isFlying())
                player.setFlying(false);

            e.setSpawnLocation(member.getEntity().getLocation());
            //player.teleport(cart.getEntity().getLocation());
            member.getEntity().setPassenger(player);
            Passenger.remove(passenger.getUUID());
        }
    }

    public static void receiveTrain(ConfigurationNode trainData) {

    }

    public static void receiveEntity(UUID uuid, EntityType entityType, CommonTagCompound entityNBT) {

    }

    public static void sendEntityToServer(LivingEntity entity, Portal targetPortal) {

    }

    public static void sendPlayerToServer(Player player, Portal targetPortal) {
        // Use PluginMessaging to send players to target server
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(targetPortal.getTargetLocation().getServer());
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public void registerActionSigns() {
        SignAction.register(signActionPortal);
        SignAction.register(signActionPortalIn);
        SignAction.register(signActionPortalOut);
    }

    public void unregisterActionSigns() {
        SignAction.unregister(signActionPortal);
        SignAction.unregister(signActionPortalIn);
        SignAction.unregister(signActionPortalOut);
    }

    public void shutdown() {
        // Close server
        if (tcpServer != null)
            tcpServer.close();

        // Close all active clients
        TCPClient.closeAll();

        unregisterActionSigns();
    }

    public TCPServer getTcpServer() {
        return tcpServer;
    }
}
