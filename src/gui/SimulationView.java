package gui;

import agents.EvacueeAgent;
import environment.Cell;
import environment.GridEnvironment;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SimulationView extends Application {
    private static GridEnvironment env;
    private Canvas canvas;
    private long simulationStartTime;
    private static final long maxDuration = 30_000; // 30 sec

    public static void setEnvironment(GridEnvironment environment) {
        env = environment;
    }

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(800, 600);
        StackPane root = new StackPane(canvas);
        stage.setScene(new Scene(root));
        stage.setTitle("Evacuation Simulation");
        stage.show();

        simulationStartTime = System.currentTimeMillis();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = System.currentTimeMillis() - simulationStartTime;
                // Case 1: Simulation reached the maximum duration
                if (elapsed >= maxDuration) {
                    System.out.println("Simulation ended: time limit reached.");
                    System.exit(0);
                }

                boolean anyFire = env.getAllAgentPositions().keySet().stream()
                        .anyMatch(id -> id.startsWith("Fire"));
                // No other fires exist
                if (!anyFire) {
                    System.out.println("Simulation ended: fire extinguished.");
                    System.exit(0);
                }

                drawGrid();
                drawAgents();
            }
        }.start();
    }

    private void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int width = env.getWidth();
        int height = env.getHeight();
        double cellWidth = canvas.getWidth() / width;
        double cellHeight = canvas.getHeight() / height;

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Cell cell = env.getCell(x, y);
                switch (cell.getType()) {
                    case FREE -> gc.setFill(Color.WHITE);
                    case WALL -> gc.setFill(Color.DARKGRAY);
                    case OBSTACLE -> gc.setFill(Color.SADDLEBROWN);
                    case EXIT -> gc.setFill(Color.LIGHTGREEN);
                }
                gc.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
            }
        }
    }

    private void drawAgents() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int width = env.getWidth();
        int height = env.getHeight();
        double cellWidth = canvas.getWidth() / width;
        double cellHeight = canvas.getHeight() / height;

        for (var entry : env.getAllAgentPositions().entrySet()) {
            String id = entry.getKey();
            int[]  pos = entry.getValue();

            // Draw firefighters
            if (id.startsWith("Firefighter")) {
                gc.setFill(Color.GOLD);
                double[] xPoints = {
                        pos[0]*cellWidth + cellWidth*0.5,
                        pos[0]*cellWidth + cellWidth*0.25,
                        pos[0]*cellWidth + cellWidth*0.75
                };
                double[] yPoints = {
                        pos[1]*cellHeight + cellHeight*0.25,
                        pos[1]*cellHeight + cellHeight*0.75,
                        pos[1]*cellHeight + cellHeight*0.75
                };
                gc.fillPolygon(xPoints, yPoints, 3);
                continue;
            }

            // Draw rescuers
            if (id.startsWith("Rescuer")) {
                gc.setFill(Color.GREEN);
                double centerX = pos[0] * cellWidth + cellWidth * 0.5;
                double centerY = pos[1] * cellHeight + cellHeight * 0.5;
                double size = Math.min(cellWidth, cellHeight) * 0.3;
                gc.setStroke(Color.GREEN);
                gc.setLineWidth(2);
                // Add horizontal line
                gc.strokeLine(centerX - size, centerY, centerX + size, centerY);
                // Add vertical line
                gc.strokeLine(centerX, centerY - size, centerX, centerY + size);
                continue;
            }

            // Draw Evacuees and fires
            EvacueeAgent.Type type = env.getAgentType(id);
            if (type == null) {
                if (id.startsWith("Evacuee") || id.startsWith("Calm") || id.startsWith("Panicked") || id.startsWith("Injured")) {
                    gc.setFill(Color.BLACK); // Dead
                } else {
                    gc.setFill(Color.DARKRED); // Fireplace
                }
            } else {
                switch (type) {
                    case CALM -> gc.setFill(Color.BLUE);
                    case PANICKED -> gc.setFill(Color.ORANGE);
                    case INJURED -> gc.setFill(Color.RED);
                }
            }

            double cx = pos[0]*cellWidth + cellWidth*0.25;
            double cy = pos[1]*cellHeight + cellHeight*0.25;
            gc.fillOval(cx, cy, cellWidth*0.5, cellHeight*0.5);
        }
    }
}
