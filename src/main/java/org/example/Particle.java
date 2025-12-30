package org.example;

public class Particle {
    double x, y;           // Position
    double vx, vy;         // Velocity
    double fx, fy;         // Force accumulator
    double charge;         // Charge (positive or negative)

    public Particle(double x, double y, double vx, double vy, double charge) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.charge = charge;
        this.fx = 0;
        this.fy = 0;
    }
}
