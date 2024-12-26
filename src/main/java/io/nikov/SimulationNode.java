package io.nikov;

import java.util.Objects;

public abstract class SimulationNode {
    public final String key;

    protected SimulationNode(String key) {
        this.key = key;
    }

    /**
     * Indicates the cost of traversing this node.
     */
    public int getAStarHeuristicCost() {
        return 0;
    }

    /**
     * Indicates when a vehicle is authorized to leave this node.
     */
    public boolean canVehicleTraverse() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimulationNode that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String toString() {
        return "SimulationNode{" +
                "key='" + key + '\'' +
                '}';
    }
}
