package io.nikov;

import org.jgrapht.graph.DefaultEdge;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class SimulationRoadSegment extends DefaultEdge {
    public final String key;
    public final int size; // In centimeters
    public final int speedLimit;
    private final AtomicLong weight;

    public SimulationRoadSegment(String key, int sizeInCm, int speedLimit) {
        super();

        this.key = key;
        this.size = sizeInCm;
        this.speedLimit = speedLimit;
        this.weight = new AtomicLong(100 + (this.size * 2L) - this.speedLimit);
    }

    public void setWeight(double weight) {
        this.weight.set((long) weight);
    }

    public double getWeight() {
        return weight.get();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimulationRoadSegment that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String toString() {
        return "SimulationRoadSegment{" +
                "key='" + key + '\'' +
                ", size=" + size +
                ", speedLimit=" + speedLimit +
                ", weight=" + weight +
                '}';
    }
}
