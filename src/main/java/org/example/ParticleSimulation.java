package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class ParticleSimulation {
    private List<Particle> particles;
    private SimulationConfig config;
    private Random random;
    private static final double DT = 0.1; // Time step
    private static final double DAMPING = 0.995; // Velocity damping (less damping)
    private static final double MIN_DISTANCE = 5; // Prevent division by zero
    private static final double FORCE_SCALE = 100.0; // Scale forces to keep particles moving
    private static final double MAX_VELOCITY = 100; // Maximum velocity cap
    private static final double frameTime = 0;

    public ParticleSimulation(SimulationConfig config) {
        this.config = config;
        this.random = new Random(config.seed);
        this.particles = new ArrayList<>();
        initializeParticles();
    }

    private void initializeParticles() {
        for (int i = 0; i < config.numParticles; i++) {
            double x = random.nextDouble() * config.width;
            double y = random.nextDouble() * config.height;
            // Increase initial velocities
            double vx = (random.nextDouble() - 0.5) * 50;
            double vy = (random.nextDouble() - 0.5) * 50;
            // Random charge magnitude between 0.5 and 2.0
            double chargeMag = 0.5 + random.nextDouble() * 1.5;
            double charge = random.nextBoolean() ? chargeMag : -chargeMag;

            particles.add(new Particle(x, y, vx, vy, charge));
        }
    }

    public void runSequential() {
        GUI gui = null;
        if (config.enableGUI) {
            gui = new GUI(config, particles);
            gui.start();
        }

        for (int cycle = 0; cycle < config.cycles; cycle++) {
            computeForces();
            updatePositions();

            if (gui != null) {
                gui.update();
            }
        }

        if (gui != null) {
            gui.stop();
        }
    }

    public void runParallel() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        GUI gui = null;
        if (config.enableGUI) {
            gui = new GUI(config, particles);
            gui.start();
        }

        for (int cycle = 0; cycle < config.cycles; cycle++) {
            computeForcesParallel(executor, numThreads);
            updatePositions();

            if (gui != null) {
                gui.update();
            }
        }

        executor.shutdown();
        if (gui != null) {
            gui.stop();
        }
    }

    public void runDistributed() {
        // Placeholder for distributed implementation
        // Would use RMI, sockets, or messaging framework
        System.out.println("Distributed mode not yet implemented");
        runParallel(); // Fall back to parallel for now
    }

    private void computeForces() {
        // Reset forces
        for (Particle p : particles) {
            p.fx = 0;
            p.fy = 0;
        }

        // Particle-particle interactions
        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                applyForce(particles.get(i), particles.get(j));
            }
        }

        // Boundary forces
        for (Particle p : particles) {
            applyBoundaryForces(p);
        }
    }

    private void computeForcesParallel(ExecutorService executor, int numThreads) {
        // Reset forces
        for (Particle p : particles) {
            p.fx = 0;
            p.fy = 0;
        }

        // Divide work among threads
        int particlesPerThread = particles.size() / numThreads;
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int start = t * particlesPerThread;
            final int end = (t == numThreads - 1) ? particles.size() : (t + 1) * particlesPerThread;

            futures.add(executor.submit(() -> {
                for (int i = start; i < end; i++) {
                    for (int j = i + 1; j < particles.size(); j++) {
                        applyForce(particles.get(i), particles.get(j));
                    }
                }
            }));
        }

        // Wait for all threads to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Apply boundary forces
        for (Particle p : particles) {
            applyBoundaryForces(p);
        }
    }

    private void applyForce(Particle p1, Particle p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double distSq = dx * dx + dy * dy;
        double dist = Math.sqrt(distSq);

        if (dist < MIN_DISTANCE) {
            dist = MIN_DISTANCE;
            distSq = dist * dist;
        }

        // Force magnitude: F = (q1 * q2) / r^2 * FORCE_SCALE
        // Same charges attract, opposite charges repel (note: opposite of typical physics)
        double forceMag = FORCE_SCALE * (p1.charge * p2.charge) / distSq;

        double fx = forceMag * (dx / dist);
        double fy = forceMag * (dy / dist);

        // Apply forces (Newton's third law)
        synchronized (p1) {
            p1.fx += fx;
            p1.fy += fy;
        }
        synchronized (p2) {
            p2.fx -= fx;
            p2.fy -= fy;
        }
    }

    private void applyBoundaryForces(Particle p) {
        double boundaryForce = config.boundaryCharge;
        double margin = 50.0;

        // Left boundary
        if (p.x < margin) {
            double dist = Math.max(p.x, 1.0);
            p.fx += boundaryForce / (dist * dist);
        }
        // Right boundary
        if (p.x > config.width - margin) {
            double dist = Math.max(config.width - p.x, 1.0);
            p.fx -= boundaryForce / (dist * dist);
        }
        // Top boundary
        if (p.y < margin) {
            double dist = Math.max(p.y, 1.0);
            p.fy += boundaryForce / (dist * dist);
        }
        // Bottom boundary
        if (p.y > config.height - margin) {
            double dist = Math.max(config.height - p.y, 1.0);
            p.fy -= boundaryForce / (dist * dist);
        }
    }

    private void updatePositions() {
        for (Particle p : particles) {
            // Update velocity: v = v + (F/m) * dt
            p.vx += p.fx * DT;
            p.vy += p.fy * DT;

            // Apply damping (less aggressive)

            // Limit velocity to MAX_VELOCITY
            double speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
            if (speed > MAX_VELOCITY) {
                double scale = MAX_VELOCITY / speed;
                p.vx *= scale;
                p.vy *= scale;
            }

            // Update position: x = x + v * dt
            p.x += p.vx * DT;
            p.y += p.vy * DT;

            // Bounce off boundaries instead of clamping
            if (p.x <= 0) {
                p.x = 0;
                p.vx = Math.abs(p.vx) * 0.8; // Lose some energy on bounce
            } else if (p.x >= config.width) {
                p.x = config.width;
                p.vx = -Math.abs(p.vx) * 0.8;
            }

            if (p.y <= 0) {
                p.y = 0;
                p.vy = Math.abs(p.vy) * 0.8;
            } else if (p.y >= config.height) {
                p.y = config.height;
                p.vy = -Math.abs(p.vy) * 0.8;
            }
        }
    }
}
