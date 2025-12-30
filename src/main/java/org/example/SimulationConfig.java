package org.example;

public class SimulationConfig  {
    SimulationMode mode = SimulationMode.PARALLEL;
    int numOfParticles = 500;
    int cycles = 10000;
    boolean enableGUI = true;
    int width = 800;
    int height = 600;
    long seed = System.currentTimeMillis();
    double boundaryCharge = 1000.0;
}
