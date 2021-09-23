package de.crafttogether.craftbahn.util;

import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.cache.RailSignCache;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.pathfinding.PathConnection;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.PathProvider;
import com.bergerkiller.bukkit.tc.pathfinding.PathRailInfo;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import de.crafttogether.CraftBahnPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;

public class SpeedData {
    private String trainName;
    private double distance;
    private String destinationName;
    private Location lastLoc;
    private double realVelocity;
    private double smoothVelocity;
    private BlockFace direction;
    private int multiplier;

    public static class TrainNotFoundException extends Exception {
        public TrainNotFoundException(String trainName) {
            super("Couldn't find '" + trainName + "'");
        }
    }

    public SpeedData(String trainName) throws TrainNotFoundException {
        MinecartGroup train = TCHelper.getTrain(trainName);
        if (train == null) throw new TrainNotFoundException(trainName);

        this.trainName = trainName;
        this.destinationName = train.getProperties().getDestination();
        this.lastLoc = train.head().getBlock().getLocation();
        this.direction = train.head().getDirection();
        calcDistance(train);
        calcVelocity(train);
    }

    public String getTrainName() {
        return trainName;
    }
    public String getDestinationName() {
        return destinationName;
    }
    public double getDistance() {
        return distance;
    }
    public double getRealVelocity() {
        return realVelocity;
    }
    public double getSmoothVelocity() { return smoothVelocity; }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setSmoothVelocity(double smoothVelocity) {
        this.smoothVelocity = smoothVelocity;
    }
    public void setRealVelocity(double realVelocity) {
        this.realVelocity = realVelocity;
    }
    
    private void calcVelocity(MinecartGroup train) {
        this.setRealVelocity(train.head().getRealSpeedLimited() * 20);
        this.setSmoothVelocity(lerp(this.getSmoothVelocity(), this.getRealVelocity(), 0.2));
    }

    private void calcDistance(MinecartGroup train) {
        if (destinationName.equals(""))
            return;

        // Determine moving direction
        Block rail = train.head().getRailTracker().getBlock();
        double distance = 0;
        double distance1 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), train.head().getDirection().getDirection()), false);
        double distance2 = -1;

        if (this.realVelocity == 0)
            distance2 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), train.head().getDirection().getOppositeFace().getDirection()), false);

        this.multiplier = 1; // ?

        if (distance1 > distance2) {
            if (distance2 > 0) {
                this.direction = train.head().getDirection().getOppositeFace();
                distance = distance2;
            } else {
                this.direction = train.head().getDirection();
                distance = distance1;
            }
        }

        if (distance2 > distance1) {
            if (distance1 > 0) {
                this.direction = train.head().getDirection();
                distance = distance1;
            } else {
                this.direction = train.head().getDirection().getOppositeFace();
                distance = distance2;
            }
        }

        // Try to determine distance to station
        double stationDistance = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), this.direction.getDirection()), true);
        if (stationDistance != -1)
            distance = stationDistance;

        if (distance == -1) {
            TCHelper.sendDebugMessage(train, "Couldn't find a node");
            distance = -1;
        }

        this.setDistance(distance);
    }

    private double getDistanceFromWalker(TrackMovingPoint walker, boolean findStation) {
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

        // Route from found node to destination
        RailState state = walker.getState();
        PathNode node = provider.getWorld(state.railWorld()).getNodeAtRail(state.railBlock());
        PathNode destination = node.getWorld().getNodeByName(this.destinationName);

        // Walk from node to destination
        if (destination != null && node != destination) {
            PathConnection[] connections = node.findRoute(destination);
            double visitedConnections = 0;

            for (int i = connections.length -1; i >= 0; i--) {
                PathConnection connection = connections[i];

                if (!findStation)
                    distance += connection.distance;

                else {
                    double stationDistance = -1;

                    Location loc = connection.destination.location.getLocation();
                    ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());
                    Component message = Component.text("Nächstes Ziel: " + connection.destination.getName()).clickEvent(tpEvent).color(NamedTextColor.DARK_GREEN);
                    TCHelper.sendDebugMessage(trainName, message);

                    BlockFace walkerDirection = TCHelper.getDirection(connection.junctionName);
                    TCHelper.sendDebugMessage(trainName, walkerDirection.name());

                    // Check 3 last connections for station
                    if (visitedConnections < 3 && connections[i - 1] != null) {
                        Message.debug("Exploring connection to " + connection.destination.getName());
                        visitedConnections++;
                        stationDistance = findStationFromWalker(new TrackMovingPoint(connections[i - 1].destination.location.getLocation(), walkerDirection.getDirection()));
                    }

                    if (stationDistance != -1) {
                        distance += stationDistance;
                        break;
                    }

                    else
                        distance += connection.distance;
                }
            }

            Location loc = node.location.getLocation();
            ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());
            Component message = Component.text("Start: " + node.getName()).clickEvent(tpEvent).color(NamedTextColor.DARK_GREEN);
            TCHelper.sendDebugMessage(trainName, message);

            return distance;
        }

        else {
            if (destination == null)
                TCHelper.sendDebugMessage(this.trainName, "Das Ziel '" + this.destinationName + "' wurde nicht gefunden");

            return -1;
        }
    }

    private double findStationFromWalker(TrackMovingPoint walker) {
        walker.setLoopFilter(true);

        double distance = 0;
        boolean stationFound = findStationSign(walker);

        while (walker.hasNext() && !stationFound) {
            walker.next();
            distance++;
            stationFound = findStationSign(walker);
        }

        if (!stationFound)
            return -1;

        return distance;
    }

    private boolean findStationSign(TrackMovingPoint walker) {
        for (RailSignCache.TrackedSign sign : walker.getState().railSigns()) {

            TCHelper.sendDebugMessage(this.trainName, "'" + sign.sign.getLine(0) + "' -> '" + sign.sign.getLine(1) + "' -> '" + sign.sign.getLine(2) + "' -> '" + sign.sign.getLine(3) + "'");

            if (!sign.sign.getLine(1).toLowerCase().startsWith("station"))
                continue;

            Location loc = sign.sign.getLocation();;
            ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());
            Component message = Component.text("Station found at: " + loc.getX() + " " + loc.getY() + " " + loc.getZ()).clickEvent(tpEvent).color(NamedTextColor.GOLD);
            TCHelper.sendDebugMessage(trainName, message);

            // Spawn markerParticle (debug)
            BlockFace facing = walker.getState().enterFace();
            Location effectLocation = sign.railBlock.getLocation()
                .add(0.5, 0.5, 0.5)
                .add(0.3 * facing.getModX(), 0.0, 0.3 * facing.getModZ());

            CraftBahnPlugin.getInstance().getSpeedometer().markerParticles.put(this.trainName, effectLocation);
            return true;
        }

        return false;
    }

    private double lerp(double a, double b, double f) {
        return a + f * (b-a);
    }

    public void update() {
        MinecartGroup train = TCHelper.getTrain(this.trainName);

        // Stop calculation if train no longer exists
        if (train == null) {
            CraftBahnPlugin.getInstance().getSpeedometer().remove(trainName);
            return;
        }

        String newName = train.getProperties().getDestination();

        if (!newName.equals(this.destinationName)) {
            setDestinationName(train.getProperties().getDestination());

            if (this.destinationName.equals("")) {
                this.distance = 0;
                return;
            }

            calcDistance(train);
        }

        calcVelocity(train);

        //Check if cart is moving forward
        BlockFace newDirection = train.head().getDirection();
        if (this.direction.getDirection().add(newDirection.getDirection()).length() <= 1) {
            this.multiplier *= -1;
        }

        this.direction = newDirection;
        Location newLoc = train.head().getBlock().getLocation();

        this.distance -= this.multiplier * newLoc.distance(this.lastLoc);
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
