package io.nikov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.GraphPath;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Vehicle {
    private static final Logger LOGGER = LogManager.getLogger();

    public final String key;
    public final String destinationNodeKey;
    public final AtomicInteger stepSpeed; // In cm/s
    private String stepStartNodeKey;
    private String stepEndNodeKey;
    public final AtomicReference<SimulationRoadSegment> stepSegment;
    public final AtomicLong stepSegmentCompletion;
    private final AtomicLong lastUpdate;
    private final AtomicBoolean destinationReached;

    public Vehicle(String key, SimulationWorld world, SimulationNode start, SimulationNode destination) {
        this.key = key;
        this.destinationNodeKey = destination.key;
        this.stepSpeed = new AtomicInteger(0);
        this.stepStartNodeKey = start.key;
        this.stepEndNodeKey = start.key;
        this.stepSegment = new AtomicReference<>();
        this.stepSegmentCompletion = new AtomicLong(0L);
        this.lastUpdate = new AtomicLong(System.currentTimeMillis());
        this.destinationReached = new AtomicBoolean(false);

        // Before starting, check if a path to the destination from the starting position exists.
        if (world.path(start, destination).getLength() == 0) {
            throw new RuntimeException("No path exists from " + start + " to " + destination);
        }
    }

    /**
     * @param newSpeed Speed must be in cm/s.
     * @return The node the vehicle is currently bounded to.
     */
    public String update(SimulationWorld world, int newSpeed) {
        // If the vehicle has arrived at the destination, we must do nothing.
        if (destinationReached.get()) {
            LOGGER.debug("Vehicle {} has arrived at destination {}. Skipping update.", this, destinationNodeKey);

            return destinationNodeKey;
        }

        // If we just started, set the first road we must do.
        if (stepSegment.get() == null) {
            setNewPath(world.pathByKeys(stepStartNodeKey, destinationNodeKey));

            LOGGER.info("Starting vehicle {} from road {}", this, stepSegment.get());
        }

        // If we just started a new segment, check if we are authorized to leave the starting node.
        if (stepSegmentCompletion.get() == 0L && !world.findNode(stepStartNodeKey).orElseThrow().canVehicleTraverse()) {
            LOGGER.debug("Vehicle {} waiting for node {} authorization. Skipping update.", this, stepStartNodeKey);

            return stepStartNodeKey;
        }

        // How much progress has the vehicle made since last update (in seconds) ?
        long progress = ((System.currentTimeMillis() - lastUpdate.get()) / 1000) * newSpeed;
        stepSegmentCompletion.addAndGet(progress);

        LOGGER.debug("Vehicle {} segment new progress is {} and total progress is {}", this, progress, stepSegmentCompletion.get());

        /*
            Has the vehicle reached the end of the road?
            If yes, set the newer node we are in.
         */
        if (hasReachedStepEndNode()) {
            // Normalize the value in case of any overflow.
            stepSegmentCompletion.set(stepSegment.get().size);

            // Are we at the end ?
            if (stepEndNodeKey.equals(destinationNodeKey)) {
                LOGGER.debug("Vehicle {} has arrived at destination {}. Stopping updates.", this, destinationNodeKey);
                destinationReached.set(true);

                return destinationNodeKey;
            }

            LOGGER.debug("Vehicle {} has reached end of road {}. Moving to next road.", this, stepSegment.get());

            setNewPath(world.pathByKeys(stepEndNodeKey, destinationNodeKey));
        }

        lastUpdate.set(System.currentTimeMillis());

        return hasReachedStepEndNode() ? stepEndNodeKey : stepStartNodeKey;
    }

    private void setNewPath(GraphPath<SimulationNode, SimulationRoadSegment> newPath) {
        // Something must have gone wrong, because we cannot reach the destination.
        if (newPath.getLength() == 0) {
            LOGGER.error("No path exists from {} to {} for vehicle {}", stepEndNodeKey, destinationNodeKey, this);
            throw new RuntimeException();
        }

        var newSegment = newPath.getEdgeList().get(0);

        LOGGER.debug("Vehicle {} new path is {} through {}", this, newPath.getVertexList(), newPath.getEdgeList());

        LOGGER.debug("Moving vehicle {} to road {}", this, newSegment);

        stepStartNodeKey = newPath.getVertexList().get(0).key;
        stepEndNodeKey = newPath.getVertexList().get(1).key;
        stepSegment.set(newSegment);
        stepSegmentCompletion.set(0L);
    }

    private boolean hasReachedStepEndNode() {
        return stepSegmentCompletion.get() >= stepSegment.get().size;
    }

    public String getStepStartNodeKey() {
        return stepStartNodeKey;
    }

    public String getStepEndNodeKey() {
        return stepEndNodeKey;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vehicle vehicle)) return false;
        return Objects.equals(key, vehicle.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "key='" + key + '\'' +
                ", destination=" + destinationNodeKey +
                ", stepSpeed=" + stepSpeed +
                ", stepStartNode=" + stepStartNodeKey +
                ", stepEndNode=" + stepEndNodeKey +
                ", stepSegment=" + stepSegment +
                ", stepSegmentCompletion=" + stepSegmentCompletion +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
