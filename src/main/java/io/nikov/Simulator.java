package io.nikov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Simulator {
    private static final Logger LOGGER = LogManager.getLogger();

    private final AtomicInteger tickCounter;
    private final ScheduledExecutorService ticksExecutor;
    private final ScheduledExecutorService tasksExecutor;
    private final SimulationWorld world;
    private final Runnable postUpdateHook;
    public final double tickRate; // iterations per second
    public final long tickRateInterval; // effective time per tick

    public Simulator(SimulationWorld world, double tickRate, Runnable postUpdateHook) {
        this.tickCounter = new AtomicInteger(0);
        this.ticksExecutor = Executors.newScheduledThreadPool(1);
        this.tasksExecutor = Executors.newScheduledThreadPool(4);
        this.world = world;
        this.tickRate = tickRate;
        this.tickRateInterval = (long) (1000 / this.tickRate);
        this.postUpdateHook = postUpdateHook;
    }

    public void start() {
        LOGGER.info("Starting simulation...");

        world.loadData();

        ticksExecutor.scheduleAtFixedRate(
                this::simulationStep,
                1000,
                tickRateInterval,
                TimeUnit.MILLISECONDS
        );

        LOGGER.info("Simulation started.");
    }

    public void stop() {
        LOGGER.info("Stopping simulation...");

        ticksExecutor.shutdownNow();
        tasksExecutor.shutdownNow();

        LOGGER.info("Simulation stopped.");
    }

    private void simulationStep() {
        LOGGER.debug("Simulation Tick: {}", tickCounter.incrementAndGet());

        world.getMap().vertexSet().forEach(simulationNode -> {
            if (simulationNode instanceof TrafficLight trafficLight) {
                trafficLight.call();
            }
        });

        LOGGER.debug("Processing vehicles...");

        world.getVehicles().forEach(vehicle -> {
            vehicle.update(world, Utilities.getRandomInt(1, 10));
        });

        LOGGER.debug("Processing vehicles complete.");

        this.tasksExecutor.submit(postUpdateHook);

        LOGGER.debug("Simulation Tick Complete.");
    }
}
