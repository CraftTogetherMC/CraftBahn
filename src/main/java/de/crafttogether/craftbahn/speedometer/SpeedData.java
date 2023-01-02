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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class SpeedData {
    private String trainName;
    private double distance;
    private String destinationName;
    private Location lastLoc;
    private double velocity;
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
    public double getVelocity() {
        return velocity;
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
    public void setVelocity(double realVelocity) {
        this.velocity = realVelocity;
    }

    private void calcVelocity(MinecartGroup train) {
        this.setVelocity(train.head().getRealSpeedLimited() * 20);
        this.setSmoothVelocity(lerp(this.getSmoothVelocity(), this.getVelocity(), 0.2));
    }

    private void calcDistance(MinecartGroup train) {
        if (destinationName.isEmpty())
            return;

        // Determine moving direction
        Block rail = train.head().getRailTracker().getBlock();
        double distance1 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), train.head().getDirection().getDirection()), false);
        double distance2 = -1;

        if (this.velocity == 0)
            distance2 = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), train.head().getDirection().getOppositeFace().getDirection()), false);

        if (distance1 > distance2) {
            if (distance2 > 0)
                this.direction = train.head().getDirection().getOppositeFace();
            else
                this.direction = train.head().getDirection();
        }

        if (distance2 > distance1) {
            if (distance1 > 0)
                this.direction = train.head().getDirection();
            else
                this.direction = train.head().getDirection().getOppositeFace();
        }

        // Try to determine distance to station
        double distance = getDistanceFromWalker(new TrackMovingPoint(rail.getLocation(), this.direction.getDirection()), true);

        if (distance == -1)
            Util.debug(train.getProperties().getTrainName(), "Couldn't find a node");

        this.setDistance(distance);
    }

    private double getDistanceFromWalker(TrackMovingPoint walker, boolean findStation) {
        PathProvider provider = TrainCarts.plugin.getPathProvider();
        walker.setLoopFilter(true);
        double distance = 0;

        // Walk until we arrive the first node
        while (provider.getRailInfo(walker.getState()) == PathRailInfo.NONE && walker.hasNext()) {
            walker.next();
            distance++;

            if (findStation) {
                Location effectLocation = walker.getState().positionBlock().getLocation().add(0, 1, 0);
                CraftBahnPlugin.plugin.getSpeedometer().getDebugParticles().add(new Speedometer.DebugParticle(trainName, effectLocation, Particle.BLOCK_MARKER, Material.GREEN_STAINED_GLASS.createBlockData()));
            }
        }

        if (!walker.hasNext()
                || provider.getRailInfo(walker.getState()) == PathRailInfo.BLOCKED
                || provider.getRailInfo(walker.getState()) != PathRailInfo.NODE) {
            return -1;
        }

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
                    Component message = Component.text("Nächstes Ziel: " + connection.destination.getName())
                            .clickEvent(tpEvent)
                            .color(NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.UNDERLINED);

                    Util.debug(trainName, message);

                    BlockFace walkerDirection = TCHelper.getDirection(connection.junctionName);
                    Util.debug(trainName, walkerDirection.name());

                    // Check 3 last connections for station
                    if (visitedConnections < 3 && connections[i -1] != null) {
                        Component from = Component.text(connections[i -1].destination.getDisplayName())
                                .clickEvent(ClickEvent.suggestCommand("/cmi tppos " + connections[i -1].destination.location.x + " " + connections[i -1].destination.location.y + " " + connections[i -1].destination.location.z))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("*klick*")))
                                .color(NamedTextColor.DARK_GREEN)
                                .decorate(TextDecoration.UNDERLINED);

                        Component to = Component.text(connection.destination.getDisplayName())
                                .clickEvent(ClickEvent.suggestCommand("/cmi tppos " + connection.destination.location.x + " " + connection.destination.location.y + " " + connection.destination.location.z))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("*klick*")))
                                .color(NamedTextColor.DARK_GREEN)
                                .decorate(TextDecoration.UNDERLINED);

                        Util.debug(trainName, Component.text("Exploring connection from ").append(from).append(Component.text(" to ")).append(to));

                        visitedConnections++;
                        stationDistance = findStationFromWalker(new TrackMovingPoint(connections[i -1].destination.location.getLocation(), walkerDirection.getDirection()));
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
                Component message = Component.text("Start: " + node.getName())
                        .clickEvent(tpEvent)
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("*klick*")))
                        .color(NamedTextColor.DARK_GREEN)
                        .decorate(TextDecoration.UNDERLINED);
                Util.debug(trainName, message);
            }
        }

        else if (node == destination)
            Util.debug(this.trainName, "Das Ziel '" + this.destinationName + "' ist der nächste Knotenpunkt");

        else {
            Util.debug(this.trainName, "Das Ziel '" + this.destinationName + "' wurde nicht gefunden");
            return -1;
        }

        return distance;
    }

    private double findStationFromWalker(TrackMovingPoint walker) {
        walker.setLoopFilter(true);

        double distance = 0;
        boolean stationFound = findStationSign(walker);
        List<Speedometer.DebugParticle> particles = new ArrayList<>();


        while (walker.hasNext() && !stationFound) {
            walker.next();
            distance++;

            Location effectLocation = walker.getState().positionBlock().getLocation().add(0, 1, 0);
            particles.add(new Speedometer.DebugParticle(trainName, effectLocation, Particle.BLOCK_MARKER, Material.GREEN_STAINED_GLASS.createBlockData()));

            stationFound = findStationSign(walker);
        }

        if (!stationFound)
            return -1;

        // Mark walked path
        CraftBahnPlugin.plugin.getSpeedometer().getDebugParticles().addAll(particles);

        return distance;
    }

    private boolean findStationSign(TrackMovingPoint walker) {
        for (RailLookup.TrackedSign sign : walker.getState().railSigns()) {

            Util.debug(this.trainName, sign.sign.getLine(0) + " / " + sign.sign.getLine(1) + " / " + sign.sign.getLine(2) + " / " + sign.sign.getLine(3));

            if (!sign.sign.getLine(1).toLowerCase().startsWith("station"))
                continue;

            Location loc = sign.sign.getLocation();
            ClickEvent tpEvent = ClickEvent.runCommand("/cmi tppos " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getWorld().getName());
            Component message = Component.text("Station found at: " + loc.getX() + " " + loc.getY() + " " + loc.getZ())
                    .clickEvent(tpEvent)
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("*klick*")))
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.UNDERLINED);
            Util.debug(trainName, message);

            // Spawn markerParticle (debug)
            Location effectLocation = sign.railBlock.getLocation().add(0, 1, 0);
            CraftBahnPlugin.plugin.getSpeedometer().getDebugParticles().add(new Speedometer.DebugParticle(trainName, effectLocation, Particle.BLOCK_MARKER, Material.BARRIER.createBlockData()));

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

        // Check if cart is moving forward
        BlockFace newDirection = train.head().getDirection();
        if (this.direction.getDirection().add(newDirection.getDirection()).length() <= 1)
            this.multiplier *= -1;

        this.direction = newDirection;
        Location newLoc = train.head().getBlock().getLocation();

        this.distance -= this.multiplier * newLoc.distance(this.lastLoc);
        if (this.distance < 0)
            this.distance = 0;

        this.lastLoc = newLoc;

        /*Util.debug(train, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        if (this.distance > 0 && !this.destinationName.equals("")) {
            TCHelper.sendMessage(train, String.format("Dein Ziel %s ist %.1f Blöcke entfernt.", this.destinationName, this.distance));
        }
        */
    }
}