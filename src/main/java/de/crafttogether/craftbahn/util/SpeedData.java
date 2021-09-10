package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.pathfinding.PathConnection;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.PathProvider;
import com.bergerkiller.bukkit.tc.pathfinding.PathRailInfo;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SpeedData {
    private Player player;
    private double distance;
    private String destinationName;
    private Location lastLoc;
    private double velocity;
    private static double distOffset = 3;

    public SpeedData(Player player) {
        this.player = player;
        this.destinationName = TCHelper.getTrain(player).getProperties().getDestination();
        this.lastLoc = TCHelper.getTrain(player).head().getBlock().getLocation();
        calcDistance();
        calcVelocity();
    }

    public Player getPlayer() {
        return player;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance - distOffset;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    private void calcVelocity() {
        MinecartGroup train = TCHelper.getTrain(this.player);
        this.setVelocity(train.head().getRealSpeedLimited() * 20);
    }

    private void calcDistance() {
        MinecartGroup train = TCHelper.getTrain(player);
        if (destinationName.equals("")) {
            return;
        }

        //Find first node from position
        Block rail = train.head().getRailTracker().getBlock();
        double distance1 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), train.head().getDirection().getDirection()));
        double distance2 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), train.head().getDirection().getOppositeFace().getDirection()));

        if (distance1 > distance2) {
            if (distance2 > 0) this.setDistance(distance2); //Check if distance2 is -1
            else this.setDistance(distance1);
            return;
        }
        if (distance2 > distance1) {
            if (distance1 > 0) this.setDistance(distance1); //Check if distance1 is -1
            else this.setDistance(distance2);
            return;
        }

        Message.debug(this.player, "Couldn't find a node");
        this.setDistance(0);

        return;
    }

    private double getDistanceFromWalker(TrackMovingPoint walker) {
        PathProvider provider = TrainCarts.plugin.getPathProvider();
        walker.setLoopFilter(true);
        double distance = 0;
        while (provider.getRailInfo(walker.getState()) != PathRailInfo.NODE && walker.hasNext()) {
            walker.next();
            distance++;
        }
        if (!walker.hasNext()) {
            return -1;
        }
        //Message.debug(player, "Node was found at:" + walker.current.getLocation().toString());
        //Route from found node to destination
        //and add all distances from connections
        RailState state = walker.getState();
        PathNode node = provider.getWorld(state.railWorld()).getNodeAtRail(state.railBlock());
        PathNode destination = node.getWorld().getNodeByName(destinationName);
        if (destination != null) {
            PathConnection[] connections = node.findRoute(destination);
            for (PathConnection connection : connections) {
                distance += connection.distance;
            }
            return distance;
        }
        Message.debug(player, "Dein Ziel wurde nicht gefunden");
        return 0;
    }

    public void update() {
        MinecartGroup train = TCHelper.getTrain(this.player);
        String newName = train.getProperties().getDestination();
        if (!newName.equals(this.destinationName)) {
            setDestinationName(TCHelper.getTrain(this.player).getProperties().getDestination());
            if (this.destinationName.equals("")) {
                this.distance = 0;
                return;
            }
            calcDistance();
        }
        calcVelocity();
        Location newLoc = train.head().getBlock().getLocation();
        this.distance -= newLoc.distance(this.lastLoc);
        if (this.distance < 0) {
            this.distance = 0;
        }
        this.lastLoc = newLoc;
        /*Message.debug(player, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        if (this.distance > 0 && !this.destinationName.equals("")) {
            this.player.spigot().sendMessage(ChatMessageType.CHAT, new TextComponent(String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance)));
        }
        */
    }
}
