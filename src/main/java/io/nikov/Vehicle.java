package io.nikov;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Vehicle implements Callable<RoadSegment> {
    public final AtomicInteger speed;
    public final AtomicReference<RoadSegment> currentSegment;
    public final AtomicInteger currentSegmentCompletion;

    public Vehicle(RoadSegment startingPosition) {
        this.speed = new AtomicInteger();
        this.currentSegment = new AtomicReference<>(startingPosition);
        this.currentSegmentCompletion = new AtomicInteger(0);
    }

    @Override
    public RoadSegment call() {
        return null;
    }
}
