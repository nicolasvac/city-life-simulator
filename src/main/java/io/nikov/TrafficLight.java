package io.nikov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TrafficLight extends SimulationNode implements Callable<TrafficLightStatus> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int CHANGE_TIME = 5000;

    private final AtomicReference<TrafficLightStatus> status;
    private final AtomicLong lastChange;

    public TrafficLight(String key) {
        super(key);

        this.status = new AtomicReference<>(EnumUtils.getRandomEnum(TrafficLightStatus.class)); // Random start
        this.lastChange = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public TrafficLightStatus call() {
        if (System.currentTimeMillis() - lastChange.get() > CHANGE_TIME) {
            this.changeStatus();
            this.lastChange.set(System.currentTimeMillis());
        }

        return status.get();
    }

    private void changeStatus() {
        LOGGER.debug("Changing status of traffic light {}", key);

        switch (status.get()) {
            case RED -> status.set(TrafficLightStatus.GREEN);
            case GREEN -> status.set(TrafficLightStatus.YELLOW);
            case YELLOW -> status.set(TrafficLightStatus.RED);
        }

        LOGGER.info("Status of traffic light {} changed to {}", key, status.get());
    }

    public TrafficLightStatus getStatus() {
        return status.get();
    }

    @Override
    public String toString() {
        return "TrafficLight{" +
                "key='" + key + '\'' +
                ", status=" + status +
                '}';
    }
}
