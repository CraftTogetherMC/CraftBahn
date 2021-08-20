package de.crafttogether.craftbahn.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import de.crafttogether.craftbahn.util.Message;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PortalHandler {

    public static void handleInput(ConfigurationNode data) {

    }

    // Handle joined player if he was a passenger
    public static void handlePassenger(Passenger passenger, PlayerSpawnLocationEvent e) {
        Player player = Bukkit.getPlayer(passenger.getUUID());

        String trainName = passenger.getTrainName();
        int cartIndex = passenger.getCartIndex();

        Message.debug("Try to find train '" + trainName + "' for " + player.getName() + " cartIndex: " + cartIndex);

        // Try to find train and set player as passenger
        MinecartGroup train = TCHelper.getTrain(trainName);

        if (train != null) {
            MinecartMember<?> cart = train.get(cartIndex);

            if (cart instanceof MinecartMemberRideable) {
                if (player.isFlying())
                    player.setFlying(false);

                e.setSpawnLocation(cart.getBlock().getLocation());
                cart.getEntity().setPassenger(player);

                Message.debug("Set player " + player.getName() + " as passenger of '" + trainName + "' at cartIndex: " + cartIndex);
                Passenger.remove(passenger.getUUID());
            }
            else
                Message.debug("Cart(" + cartIndex + ") at Train '" + trainName + "' is not rideable.");
        }
        else
            Message.debug("Train '" + trainName + "' was not found.");
    }
}
