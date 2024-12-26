package io.nikov;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Simulator simulator;
    private final Set<WsContext> javalinClients = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Gson gson = new Gson();

    public static void main(String[] args) {
        new Main().run();
    }

    public Main() {
        // Initialize Simulation World and Simulator
        SimulationWorld world = new SimulationWorld();
        this.simulator = new Simulator(world, 5, () -> broadcastSimulationState(world));
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(simulator::stop));

        simulator.start();

        // Create Javalin instance
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public"); // Serve the static files for the frontend
        }).start(8080);

        // WebSocket Endpoint
        app.ws("/simulation", ws -> {
            ws.onConnect(ctx -> {
                javalinClients.add(ctx);
                LOGGER.debug("Client Connected: {}", ctx.sessionId());
            });

            ws.onClose(ctx -> {
                javalinClients.remove(ctx);
                LOGGER.debug("Client Disconnected: {}", ctx.sessionId());
            });

            ws.onMessage(ctx -> {
                LOGGER.info("Message Received: {}", ctx.message());
                // Could add more functionality here, like pausing simulation, etc.
            });
        });
    }

    private void broadcastSimulationState(SimulationWorld world) {
        // Serialize graph and vehicles into JSON
        SimulationWebSocketState state = new SimulationWebSocketState(world.getMap(), world.getVehicles());
        String json = gson.toJson(state);

        // Send state to all connected WebSocket clients
        for (WsContext client : javalinClients) {
            client.send(json);
        }
    }

    static class SimulationWebSocketState {
        public Object[] nodes;
        public Object[] edges;
        public Object[] vehicles;

        public SimulationWebSocketState(DirectedWeightedMultigraph<SimulationNode, SimulationRoadSegment> map, ArrayList<Vehicle> vehicles) {
            // Convert graph nodes to a simple JSON structure
            this.nodes = map.vertexSet().stream()
                    .map(node -> new Node(node.key))
                    .toArray();

            // Convert graph edges to a simple JSON structure
            this.edges = map.edgeSet().stream()
                    .map(edge -> new Edge(
                            map.getEdgeSource(edge).key,
                            map.getEdgeTarget(edge).key,
                            edge.key,
                            edge.size
                    ))
                    .toArray();

            // Convert vehicles to a simple JSON structure
            this.vehicles = vehicles.stream()
                    .map(vehicle -> new VehicleState(
                            vehicle.key,
                            vehicle.getStepStartNodeKey(),
                            vehicle.getStepEndNodeKey(),
                            vehicle.stepSegmentCompletion.get()
                    ))
                    .toArray();
        }

        private static class Node {
            public String id;

            public Node(String id) {
                this.id = id;
            }
        }

        private static class Edge {
            public String source;
            public String target;
            public String id;
            public long size;

            public Edge(String source, String target, String id, long size) {
                this.source = source;
                this.target = target;
                this.id = id;
                this.size = size;
            }
        }

        private static class VehicleState {
            public String id;
            public String startNode;
            public String endNode;
            public long progress;

            public VehicleState(String id, String startNode, String endNode, long progress) {
                this.id = id;
                this.startNode = startNode;
                this.endNode = endNode;
                this.progress = progress;
            }
        }
    }
}