package io.nikov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.ArrayList;
import java.util.Optional;

public class SimulationWorld {
    private static final Logger LOGGER = LogManager.getLogger();

    private final DirectedWeightedMultigraph<SimulationNode, SimulationRoadSegment> map;
    private final AStarShortestPath<SimulationNode, SimulationRoadSegment> aStar;
    private final ArrayList<Vehicle> vehicles;

    public SimulationWorld() {
        this.map = new DirectedWeightedMultigraph<>(SimulationRoadSegment.class);
        this.aStar = new AStarShortestPath<>(
                this.map,
                (simulationNode, v1) -> simulationNode.getAStarHeuristicCost() + v1.getAStarHeuristicCost()
        );
        this.vehicles = new ArrayList<>();
    }

    public void loadData() {
        LOGGER.info("Loading data...");

        var sem1 = new TrafficLight("sem1");
        var sem2 = new TrafficLight("sem2");
        var sem3 = new TrafficLight("sem3");
        var sem4 = new TrafficLight("sem4");

        map.addVertex(sem1);
        map.addVertex(sem2);
        map.addVertex(sem3);
        map.addVertex(sem4);

        map.addEdge(sem1, sem2, new SimulationRoadSegment("s1", 50, 50));
        map.addEdge(sem1, sem3, new SimulationRoadSegment("s2", 30, 130));
        map.addEdge(sem2, sem3, new SimulationRoadSegment("s3", 180, 90));
        map.addEdge(sem3, sem4, new SimulationRoadSegment("s4", 40, 90));

        vehicles.add(new Vehicle(
                "v1",
                this,
                sem1,
                sem4
        ));

        LOGGER.info("Data loaded.");
    }

    public GraphPath<SimulationNode, SimulationRoadSegment> path(SimulationNode start, SimulationNode end) {
        return aStar.getPath(start, end);
    }

    public GraphPath<SimulationNode, SimulationRoadSegment> pathByKeys(String startKey, String endKey) {
        return aStar.getPath(new KeyableNode(startKey), new KeyableNode(endKey));
    }

    public Optional<SimulationNode> findNode(String key) {
        return map.vertexSet().stream()
                .filter(node -> key.equals(node.key))
                .findFirst();
    }

    public DirectedWeightedMultigraph<SimulationNode, SimulationRoadSegment> getMap() {
        return map;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }
}
