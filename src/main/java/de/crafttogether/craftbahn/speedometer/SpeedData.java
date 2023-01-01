package de.crafttogether.craftbahn.speedometer;

import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.cache.RailSignCache;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.pathfinding.PathConnection;
import com.bergerkiller.bukkit.tc.pathfinding.PathNode;
import com.bergerkiller.bukkit.tc.pathfinding.PathProvider;
import com.bergerkiller.bukkit.tc.pathfinding.PathRailInfo;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.TCHelper;
import de.crafttogether.craftbahn.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SpeedData {
    private String trainName;
    private double distance;
    private String destinationName;
    private Location lastLoc;
    private double realVelocity;
    private double smoothVelocity;
    private BlockFace direction;
    private int multiplier;

    public SpeedData(String trainName) {
        MinecartGroup train = TCHelper.getTrain(trainName);
        if (train == null) return;

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
            Util.debug(train.getProperties().getTrainName(), "Couldn't find a node");
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
                    Util.debug(trainName, message);

                    BlockFace walkerDirection = TCHelper.getDirection(connection.junctionName);
                    Util.debug(trainName, walkerDirection.name());

                    // Check 3 last connections for station
                    if (visitedConnections < 3 && connections[i - 1] != null) {
                        Util.debug(trainName, Component.text("Exploring connection from " + connections[i - 1].destination.getName() + " to " + connection.destination.getName()).color(NamedTextColor.YELLOW));
                        visitedConnections++;
                        stationDistance = findStationFromWalker(new TrackMovingPoint(connections[i - 1].destination.location.getLocation(), walkerDirection.getDirection()));
                    }

                    if (stationDistance != -1) {
                        distance += stationDistance;
                        return distance;
                    }

                    else
                        distance += connection.distance;
                }
            }

            if (findStation) {
                Location loc = node.location.getLocation();
                ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());
                Component message = Component.text("Start: " + node.getName()).clickEvent(tpEvent).color(NamedTextColor.DARK_GREEN);
                Util.debug(trainName, message);
            }

            return distance;
        }

        else {
            if (destination == null)
                Util.debug(this.trainName, "Das Ziel '" + this.destinationName + "' wurde nicht gefunden");

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
        for (RailLookup.TrackedSign sign : walker.getState().railSigns()) {

            Util.debug(this.trainName, sign.sign.getLine(0) + " / " + sign.sign.getLine(1) + " / " + sign.sign.getLine(2) + " / " + sign.sign.getLine(3));

            if (!sign.sign.getLine(1).toLowerCase().startsWith("station"))
                continue;

            Location loc = sign.sign.getLocation();;
            ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());
            Component message = Component.text("Station found at: " + loc.getX() + " " + loc.getY() + " " + loc.getZ()).clickEvent(tpEvent).color(NamedTextColor.GOLD);
            Util.debug(trainName, message);

            // Spawn markerParticle (debug)
            BlockFace facing = walker.getState().enterFace();
            Location effectLocation = sign.railBlock.getLocation()
                    .add(1, 1, 1)
                    .add(0.3 * facing.getModX(), 0.0, 0.3 * facing.getModZ());
            CraftBahnPlugin.plugin.getSpeedometer().getParticleLocations().put(this.trainName, effectLocation);

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
            CraftBahnPlugin.plugin.getSpeedometer().remove(trainName);
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

        /*Util.debug(train, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        if (this.distance > 0 && !this.destinationName.equals("")) {
            TCHelper.sendMessage(train, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        }
        */
    }
}