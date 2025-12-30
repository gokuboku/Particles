package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GUI extends JFrame {
    private JPanel panel;
    private List<Particle> particles;
    private SimulationConfig config;
    private volatile boolean running = true;
    private Thread renderThread;
    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000 / FPS;
    private static int currentFps = 0;
    private static int fpsCounter = 0;
    private static long startTime = 0;

    public GUI(SimulationConfig config, List<Particle> particles) {
        this.config = config;
        this.particles = particles;

        setTitle("Particle Simulation");
        setSize(config.width, config.height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new DrawPanel();
        add(panel);


        setVisible(true);
    }

    public void start() {
        renderThread = new Thread(() -> {
            while (running) {
                long startTime = System.currentTimeMillis();

                panel.repaint();

                long elapsed = System.currentTimeMillis() - startTime;
                long sleepTime = FRAME_TIME - elapsed;
                while (System.currentTimeMillis() - startTime < FRAME_TIME) {

                }
//                if (sleepTime > 0) {
//                    try {
//                        Thread.sleep(sleepTime);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
            }
        });
        renderThread.start();
    }

    public void stop() {
        running = false;
        try {
            renderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void update() {
        long endTime = System.currentTimeMillis();
        fpsCounter++;
        if(endTime - startTime >= 1000) {
            currentFps = fpsCounter;
            startTime = System.currentTimeMillis();
            fpsCounter = 0;
        }
        // This method can be called to signal updates, but rendering is independent
    }

    class DrawPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // Draw background
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
            // Draw particles
            synchronized (particles) {
                for (Particle p : particles) {
                    if (p.charge > 0) {
                        g2.setColor(Color.RED);
                    } else {
                        g2.setColor(Color.BLUE);
                    }

                    int size = 6;
                    g2.fillOval((int) p.x - size / 2, (int) p.y - size / 2, size, size);
                }
            }
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("serif", Font.BOLD, 30));
            g2.drawString("FPS:" +String.valueOf(currentFps),680, 25);
        }
    }
}