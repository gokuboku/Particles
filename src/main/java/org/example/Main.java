package org.example;

import Utils.Logger;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        // Parse command line arguments
        SimulationConfig config = parseArgs(args);

        // Create simulation
        ParticleSimulation sim = new ParticleSimulation(config);

        // Run simulation based on mode
        long startTime = System.currentTimeMillis();

        if(config.mode == SimulationMode.SEQUENTIAL){
            sim.runSequential();
        }
        else if (config.mode == SimulationMode.PARALLEL){
            sim.runParallel();
        }
        else if (config.mode == SimulationMode.DISTRIBUTED){
            sim.runDistributed();
        }

        long endTime = System.currentTimeMillis();

        Logger.info("Simulation completed in " + (endTime - startTime) + " ms");
        Logger.info("Cycles: " + config.cycles);
        Logger.info("Particles: " + config.numParticles);
    }

    private static SimulationConfig parseArgs(String[] args) {
        SimulationConfig config = new SimulationConfig();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--mode":
                    config.mode = SimulationMode.valueOf(args[++i].toUpperCase());
                    break;
                case "--particles":
                    config.numParticles = Integer.parseInt(args[++i]);
                    break;
                case "--cycles":
                    config.cycles = Integer.parseInt(args[++i]);
                    break;
                case "--gui":
                    config.enableGUI = Boolean.parseBoolean(args[++i]);
                    break;
                case "--width":
                    config.width = Integer.parseInt(args[++i]);
                    break;
                case "--height":
                    config.height = Integer.parseInt(args[++i]);
                    break;
                case "--seed":
                    config.seed = Long.parseLong(args[++i]);
                    break;
            }
        }

        return config;
    }
}