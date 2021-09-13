package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.cache.RailSignCache;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.pathfinding.PathConnection;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.PathProvider;
import com.bergerkiller.bukkit.tc.pathfinding.PathRailInfo;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class SpeedData {
    private MinecartGroup train;
    private double distance;
    private String destinationName;
    private Location lastLoc;
    private double realVelocity;
    private double smoothVelocity;
    private Vector direction;
    private int multiplicator;
    private static double distOffset = 3;

    public SpeedData(MinecartGroup train) {
        this.train = train;
        this.destinationName = train.getProperties().getDestination();
        this.lastLoc = train.head().getBlock().getLocation();
        this.direction = train.head().getDirection().getDirection().normalize();
        calcDistance();
        calcVelocity();
    }

    public MinecartGroup getTrain() {
        return train;
    }

    public double getRealVelocity() {
        return realVelocity;
    }

    public void setRealVelocity(double realVelocity) {
        this.realVelocity = realVelocity;
    }

    public double getSmoothVelocity() { return smoothVelocity; }

    public void setSmoothVelocity(double smoothVelocity) {
        this.smoothVelocity = smoothVelocity;
    }

    public double getDistance() {
        return distance;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setTrain(MinecartGroup train) {
        this.train = train;
    }


    public void setDistance(double distance) {
        this.distance = distance - distOffset;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    private void calcVelocity() {
        this.setRealVelocity(this.train.head().getRealSpeedLimited() * 20);
        this.setSmoothVelocity(lerp(this.getSmoothVelocity(), this.getRealVelocity(), 0.2));
    }

    private void calcDistance() {
        if (destinationName.equals("")) {
            return;
        }

        //Find first node from position
        Block rail = this.train.head().getRailTracker().getBlock();
        double distance1 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), this.train.head().getDirection().getDirection()));
        double distance2 = -1;
        if (this.realVelocity == 0) {
            distance2 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), this.train.head().getDirection().getOppositeFace().getDirection()));
        }
        PathProvider provider = TrainCarts.plugin.getPathProvider();
        PathNode destination = provider.getWorld(rail.getWorld()).getNodeByName(destinationName);

        double offset1 = findStationFromWalker(new TrackMovingPoint(destination.location.getLocation(), new Vector(1, 0, 0)));
        double offset2 = findStationFromWalker(new TrackMovingPoint(destination.location.getLocation(), new Vector(-1, 0, 0)));
        double offset3 = findStationFromWalker(new TrackMovingPoint(destination.location.getLocation(), new Vector(0, 0, 1)));
        double offset4 = findStationFromWalker(new TrackMovingPoint(destination.location.getLocation(), new Vector(0, 0, -1)));
        double offsets[] = {offset1, offset2, offset3, offset4};

        Message.debug(String.format("%.0f %.0f %.0f %.0f", offset1, offset2, offset3, offset4));

        Arrays.sort(offsets);
        int idx = -1;
        for (double offset : offsets) {
            idx++;
            if (offset < 0) continue;
            break;
        }
        distance1 -= offsets[idx];
        distance2 -= offsets[idx];
        this.multiplicator = 1;

        if (distance1 > distance2) {
            if (distance2 > 0) {
                this.setDistance(distance2); //Check if distance2 is -1
                this.direction = train.head().getDirection().getOppositeFace().getDirection().normalize();
            } else {
                this.setDistance(distance1);
                this.direction = train.head().getDirection().getDirection().normalize();
            }
            return;
        }

        if (distance2 > distance1) {
            if (distance1 > 0) {
                this.setDistance(distance1); //Check if distance1 is -1
                this.direction = train.head().getDirection().getDirection().normalize();
            } else {
                this.setDistance(distance2);
                this.direction = train.head().getDirection().getOppositeFace().getDirection().normalize();
            }
            return;
        }

        TCHelper.sendDebugMessage(train, "Couldn't find a node");
        this.setDistance(0);
    }

    private double getDistanceFromWalker(TrackMovingPoint walker) {
        PathProvider provider = TrainCarts.plugin.getPathProvider();
        walker.setLoopFilter(true);
        double distance = 0;

        while (provider.getRailInfo(walker.getState()) == PathRailInfo.NONE && walker.hasNext()) {
            walker.next();
            distance++;
        }

        if (!walker.hasNext() || provider.getRailInfo(walker.getState()) == PathRailInfo.BLOCKED) {
            return -1;
        }

        //Message.debug(player, "Node was found at:" + walker.current.getLocation().toString());
        //Route from found node to destination
        //and add all distances from connections
        RailState state = walker.getState();
        PathNode node = provider.getWorld(state.railWorld()).getNodeAtRail(state.railBlock());
        PathNode destination = node.getWorld().getNodeByName(destinationName);

        if (destination != null) {
            if (node != destination) {
                PathConnection[] connections = node.findRoute(destination);
                for (PathConnection connection : connections) {
                    distance += connection.distance;
                }
            }

            return distance;
        }

        TCHelper.sendDebugMessage(train, "Dein Ziel wurde nicht gefunden");
        return 0;
    }

    private double findStationFromWalker(TrackMovingPoint walker) {
        PathProvider provider = TrainCarts.plugin.getPathProvider();
        walker.setLoopFilter(true);
        double distance = 0;
        //Check if same block has signs
        boolean stationFound = checkForSignTypeFromWalker(walker, "station");

        while (walker.hasNext() && !stationFound && distance < 50) {
            walker.next();
            distance++;
            stationFound = checkForSignTypeFromWalker(walker, "station");
        }
        if (!stationFound) return -1;

        return distance;
    }

    private boolean checkForSignTypeFromWalker(TrackMovingPoint walker, String signType) {
        for (RailSignCache.TrackedSign sign : walker.getState().railSigns()) {
            //Message.debug(this.player, "\'" + sign.sign.getLine(1) + "\'");
            if (sign.sign.getLine(1).contains(signType)) {
                TCHelper.sendDebugMessage(this.train, String.format("SignType \"%s\" found", signType));
                return true;
            }
        }
        return false;
    }

    private double lerp(double a, double b, double f){
        return a + f * (b-a);
    }
    public void update() {
        String newName = this.train.getProperties().getDestination();

        if (!newName.equals(this.destinationName)) {
            setDestinationName(this.train.getProperties().getDestination());

            if (this.destinationName.equals("")) {
                this.distance = 0;
                return;
            }
            calcDistance();
        }

        calcVelocity();

        //Check if cart is moving forward
        Vector newDirection = train.head().getDirection().getDirection().normalize();
        if (this.direction.add(newDirection).length() <= 1) {
            this.multiplicator *= -1;
        }

        this.direction = newDirection;
        Location newLoc = train.head().getBlock().getLocation();

        this.distance -= this.multiplicator * newLoc.distance(this.lastLoc);
        if (this.distance < 0) {
            this.distance = 0;
        }
        this.lastLoc = newLoc;

        /*TCHelper.sendDebugMessage(train, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        if (this.distance > 0 && !this.destinationName.equals("")) {
            TCHelper.sendMessage(train, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        }
        */
    }
}