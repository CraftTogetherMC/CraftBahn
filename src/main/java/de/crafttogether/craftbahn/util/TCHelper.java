package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberChest;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;
import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.localization.PlaceholderResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TCHelper {

    public static MinecartGroup getTrain(Player p) {
        Entity entity = p.getVehicle();
        MinecartMember<?> member = null;

        if (entity == null)
            return null;

        if (entity instanceof Minecart)
            member = MinecartMemberStore.getFromEntity(entity);

        if (member != null)
            return member.getGroup();

        return null;
    }

    public static MinecartGroup getTrain(String trainName) {
        TrainProperties trainProperties = TrainPropertiesStore.get(trainName);
        return (trainProperties == null) ? null : trainProperties.getHolder();
    }

    public static List<Player> getPlayerPassengers(MinecartMember<?> member) {
        List<Player> passengers = new ArrayList<>();
        for (Entity passenger : member.getEntity().getEntity().getPassengers())
            if (passenger instanceof Player) passengers.add((Player) passenger);

        return passengers;
    }

    public static List<Player> getPlayerPassengers(MinecartGroup group) {
        List<Player> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPlayerPassengers(member));

        return passengers;
    }

    public static List<Entity> getPassengers(MinecartMember<?> member) {
        return member.getEntity().getPassengers();
    }

    public static List<Entity> getPassengers(MinecartGroup group) {
        List<Entity> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPassengers(member));

        return passengers;
    }

    public static boolean hasTagIgnoreCase(String tag, Collection<String> tags) {
        for (String found : tags)
            if (found.equalsIgnoreCase(tag)) return true;

        return false;
    }

    public static void clearInventory(MinecartMember<?> member) {
        if (member instanceof MinecartMemberChest chestCart) {
            chestCart.getEntity().getInventory().clear();
        }
    }

    public static void clearInventory(MinecartGroup group) {
        for (MinecartMember<?> member : group)
            clearInventory(member);
    }

    public static void sendMessage(MinecartMember<?> member, Localization localization, PlaceholderResolver... arguments) {
        for (Player passenger : getPlayerPassengers(member))
            localization.message(passenger, arguments);
    }

    public static void sendMessage(MinecartGroup group, Localization localization, PlaceholderResolver... arguments) {
        for (Player passenger : getPlayerPassengers(group))
            localization.message(passenger, arguments);
    }

    public static void sendMessage(MinecartMember<?> member, Component message) {
        for (Player passenger : getPlayerPassengers(member))
            passenger.sendMessage(message);
    }

    public static void sendMessage(MinecartGroup group, Component message) {
        for (Player passenger : getPlayerPassengers(group))
            passenger.sendMessage(message);
    }

    public static SpawnableGroup.SpawnLocationList getSpawnLocations(SpawnableGroup spawnable, RailPiece rail, Sign sign) {
        SignActionEvent info = new SignActionEvent(sign.getBlock());

        /*
          Copyright (C) 2013-2022 bergerkiller
        */

        if (spawnable.getMembers().isEmpty())
            return null;

        // Find the movement direction vector on the rails
        // This, and the inverted vector, are the two directions in which can be spawned
        Vector railDirection;
        {
            RailState state = RailState.getSpawnState(rail);
            railDirection = state.motionVector();
        }

        // Figure out a preferred direction to spawn into, and whether to allow centering or not
        // This is defined by:
        // - Watched directions ([train:right]), which disables centering
        // - Which block face of the sign is powered, which disables centering
        // - Facing of the sign if no direction is set, which enables centering
        boolean isBothDirections;
        boolean useCentering;

        Vector spawnDirection;
        {
            boolean spawnA = info.isWatchedDirection(railDirection.clone().multiply(-1.0));
            boolean spawnB = info.isWatchedDirection(railDirection);

            if (isBothDirections = (spawnA && spawnB)) {
                // Decide using redstone power if both directions are watched
                BlockFace face = com.bergerkiller.bukkit.tc.Util.vecToFace(railDirection, false);
                spawnA = info.isPowered(face);
                spawnB = info.isPowered(face.getOppositeFace());
            }

            if (spawnA && !spawnB) {
                // Definitively into spawn direction A
                spawnDirection = railDirection;
                useCentering = false;
            }

            else if (!spawnA && spawnB) {
                // Definitively into spawn direction B
                spawnDirection = railDirection.clone().multiply(-1.0);
                useCentering = false;
            }

            else {
                // No particular direction is decided
                // Center the train and spawn relative right of the sign
                if (FaceUtil.isVertical(com.bergerkiller.bukkit.tc.Util.vecToFace(railDirection, false))) {
                    // Vertical rails, launch downwards
                    if (railDirection.getY() < 0.0)
                        spawnDirection = railDirection;
                    else
                        spawnDirection = railDirection.clone().multiply(-1.0);
                }

                else {
                    // Horizontal rails, launch most relative right of the sign facing
                    Vector facingDir = FaceUtil.faceToVector(FaceUtil.rotate(info.getFacing(), -2));
                    if (railDirection.dot(facingDir) >= 0.0)
                        spawnDirection = railDirection;
                    else
                        spawnDirection = railDirection.clone().multiply(-1.0);
                }
                useCentering = true;
            }
        }

        // If a center mode is defined in the declared spawned train, then adjust the
        // centering rule accordingly.
        if (spawnable.getCenterMode() == SpawnableGroup.CenterMode.MIDDLE)
            useCentering = false; // No centering

        else if (spawnable.getCenterMode() == SpawnableGroup.CenterMode.LEFT || spawnable.getCenterMode() == SpawnableGroup.CenterMode.RIGHT)
            useCentering = false;

        // If CenterMode is LEFT, then we use the REVERSE spawn mode instead of DEFAULT
        // This places the head close to the sign, rather than the tail
        SpawnableGroup.SpawnMode directionalSpawnMode = SpawnableGroup.SpawnMode.DEFAULT;
        /*
        if (spawnable.getCenterMode() == SpawnableGroup.CenterMode.LEFT) {
            directionalSpawnMode = SpawnableGroup.SpawnMode.REVERSE;
        }*/

        // Attempt spawning the train in priority of operations
        SpawnableGroup.SpawnLocationList spawnLocations = null;
        if (useCentering) {
            // First try spawning it centered, facing in the suggested spawn direction
            spawnLocations = spawnable.findSpawnLocations(info.getRailPiece(), spawnDirection, SpawnableGroup.SpawnMode.CENTER);

            // If this hits a dead-end, in particular with single-cart spawns, try the opposite direction
            if (spawnLocations != null && !spawnLocations.can_move) {
                Vector opposite = spawnDirection.clone().multiply(-1.0);
                SpawnableGroup.SpawnLocationList spawnOpposite = spawnable.findSpawnLocations(
                        info.getRailPiece(), opposite, SpawnableGroup.SpawnMode.CENTER);

                if (spawnOpposite != null && spawnOpposite.can_move) {
                    spawnDirection = opposite;
                    spawnLocations = spawnOpposite;
                }
            }
        }

        // First try the suggested direction
        if (spawnLocations == null)
            spawnLocations = spawnable.findSpawnLocations(info.getRailPiece(), spawnDirection, directionalSpawnMode);

        // Try opposite direction if not possible
        // If movement into this direction is not possible, and both directions
        // can be spawned (watched directions), also try other direction.
        // If that direction can be moved into, then use that one instead.
        if (spawnLocations == null || (!spawnLocations.can_move && isBothDirections)) {
            Vector opposite = spawnDirection.clone().multiply(-1.0);
            SpawnableGroup.SpawnLocationList spawnOpposite = spawnable.findSpawnLocations(
                    info.getRailPiece(), opposite, directionalSpawnMode);

            if (spawnOpposite != null && (spawnLocations == null || spawnOpposite.can_move)) {
                spawnDirection = opposite;
                spawnLocations = spawnOpposite;
            }
        }

        // If still not possible, try centered if we had not tried yet, just in case
        if (spawnLocations == null && !useCentering)
            spawnLocations = spawnable.findSpawnLocations(info.getRailPiece(), spawnDirection, SpawnableGroup.SpawnMode.CENTER);

        return spawnLocations;
    }
}