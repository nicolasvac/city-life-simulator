package io.nikov;

import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RoadSegment extends DefaultEdge {
    public final int size;
    private final AtomicLong weight;
    public final AtomicReference<ArrayList<Vehicle>> vehicles;

    public RoadSegment(int size) {
        super();

        this.size = size;
        this.weight = new AtomicLong(size * 2L);
        this.vehicles = new AtomicReference<>(new ArrayList<>());
    }

    public void setWeight(double weight) {
        this.weight.set((long) weight);
    }

    public double getWeight() {
        return weight.get();
    }

    @Override
    public String toString() {
        return "RoadSegment{" +
                "size=" + size +
                ", weight=" + weight +
                '}';
    }
}
