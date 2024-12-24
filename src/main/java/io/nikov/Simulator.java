package io.nikov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Simulator {
    private static final Logger LOGGER = LogManager.getLogger();

    private final AtomicInteger tickCounter;
    private final ScheduledExecutorService ticksExecutor;
    private final ScheduledExecutorService tasksExecutor;
    private final DirectedWeightedMultigraph<SimulationNode, RoadSegment> map;

    private static final int TICK_RATE = 20; // iterations per second
    private static final long TICK_INTERVAL_MS = 1000 / TICK_RATE; // effective time per tick

    public Simulator() {
        this.tickCounter = new AtomicInteger(0);
        this.ticksExecutor = Executors.newScheduledThreadPool(1);
        this.tasksExecutor = Executors.newScheduledThreadPool(4);
        this.map = new DirectedWeightedMultigraph<>(RoadSegment.class);
    }

    public void start() {
        LOGGER.info("Starting simulation...");

        this.ticksExecutor.scheduleAtFixedRate(
                this::simulationStep,
                1000,
                TICK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );

        this.tasksExecutor.scheduleAtFixedRate(
                this::aStarTest,
                1,
                6,
                TimeUnit.SECONDS
        );

        LOGGER.info("Simulation started.");
    }

    public void stop() {
        LOGGER.info("Stopping simulation...");

        this.ticksExecutor.shutdownNow();
        this.tasksExecutor.shutdownNow();

        LOGGER.info("Simulation stopped.");
    }

    public void loadData() throws InterruptedException {
        LOGGER.info("Loading data...");

        var sem1 = new TrafficLight("sem1");
        Thread.sleep(1);
        var sem2 = new TrafficLight("sem2");
        Thread.sleep(1);
        var sem3 = new TrafficLight("sem3");
        Thread.sleep(1);
        var sem4 = new TrafficLight("sem4");
        Thread.sleep(1);

        this.map.addVertex(sem1);
        this.map.addVertex(sem2);
        this.map.addVertex(sem3);
        this.map.addVertex(sem4);

        this.map.addEdge(sem1, sem2, new RoadSegment(1));
        this.map.addEdge(sem1, sem3, new RoadSegment(3));
        this.map.addEdge(sem2, sem3, new RoadSegment(1));
        this.map.addEdge(sem3, sem4, new RoadSegment(1));

        LOGGER.info("Data loaded.");
    }

    private void simulationStep() {
        LOGGER.debug("Simulation Tick: {}", tickCounter.incrementAndGet());

        LOGGER.debug("Processing traffic light changes...");

        this.map.vertexSet().forEach(simulationNode -> {
            if (simulationNode instanceof TrafficLight trafficLight) {
                trafficLight.call();
            }
        });

        LOGGER.debug("Traffic light changes processed.");

        LOGGER.debug("Simulation Tick Complete.");
    }

    public DirectedWeightedMultigraph<SimulationNode, RoadSegment> getMap() {
        return this.map;
    }

    private void aStarTest() {
        AStarAdmissibleHeuristic<SimulationNode> heuristic = new AStarAdmissibleHeuristic<>() {
            @Override
            public double getCostEstimate(SimulationNode simulationNode, SimulationNode v1) {
                if (simulationNode instanceof TrafficLight trafficLight) {
                    return switch (trafficLight.getStatus()) {
                        case RED -> 3;
                        case YELLOW -> 2;
                        case GREEN -> 1;
                    };
                }

                return 0;
            }
        };

        AStarShortestPath<SimulationNode, RoadSegment> aStar = new AStarShortestPath<>(this.map, heuristic);

        LOGGER.info("AStar Path SEM1 -> SEM4: {}", aStar.getPath(new TrafficLight("sem1"), new TrafficLight("sem4")).getEdgeList());
    }
}
