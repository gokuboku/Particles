package org.example;

import Utils.Logger;

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
        Logger.info("Particles: " + config.numOfParticles);

        if(System.currentTimeMillis() - endTime < 100000){
            try{
                Thread.sleep(100000);
            }
            catch(Exception e){
                Logger.error(e.getMessage());
            }
        }
        System.exit(0);
    }

    private static SimulationConfig parseArgs(String[] args) {
        SimulationConfig config = new SimulationConfig();

        for (int i = 0; i < args.length; i += 2) {
            if(args[i].equals("--mode")){
                config.mode = SimulationMode.valueOf(args[i+1].toUpperCase());
            }
            else if(args[i].equals("--particles")){
                config.numOfParticles = Integer.parseInt(args[i+1]);
            }
            else if(args[i].equals("--cycles")){
                config.cycles = Integer.parseInt(args[i+1]);
            }
            else if(args[i].equals("--gui")){
                config.enableGUI = Boolean.parseBoolean(args[i+1]);
            }
            else if(args[i].equals("--width")){
                config.width = Integer.parseInt(args[i+1]);
            }
            else if(args[i].equals("--height")){
                config.height = Integer.parseInt(args[i+1]);
            }
            else if(args[i].equals("--seed")){
                config.seed = Long.parseLong(args[i+1]);
            }
        }

        return config;
    }
}