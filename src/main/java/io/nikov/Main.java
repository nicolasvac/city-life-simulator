package io.nikov;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Simulator simulation;

    public static void main(String[] args) throws InterruptedException {
        new Main().run();
    }

    public Main() {
        this.simulation = new Simulator();
    }

    public void run() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(simulation::stop));

        simulation.loadData();
        simulation.start();
    }
}