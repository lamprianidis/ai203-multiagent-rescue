package gui;

import agents.EvacueeAgent;
import agents.manager.AgentManager;
import agents.manager.AgentSettings;
import jade.wrapper.StaleProxyException;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import environment.Cell;
import environment.GridEnvironment;

public class SimulationView extends Application {
    private static GridEnvironment env;
    private static AgentSettings settings;

    private Canvas canvas;
    private long simulationStartTime;
    private AnimationTimer timer;
    private static final long maxDuration = 30_000; // 30 sec

    public static void setEnvironment(GridEnvironment environment) {
        env = environment;
    }

    public static void setAgentSettings(AgentSettings agentSettings) {
        settings = agentSettings;
    }

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(1200, 900);

        Label controlsLabel = new Label("Simulation Controls");
        controlsLabel.setStyle("-fx-font-weight: bold");

        Button startBtn = new Button("Start Simulation");
        VBox leftPane = new VBox(10, controlsLabel, startBtn);
        leftPane.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setLeft(leftPane);

        stage.setScene(new Scene(root));
        stage.setTitle("Evacuation Simulation");
        stage.show();

        simulationStartTime = System.currentTimeMillis();

        timer = new AnimationTimer() {
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
        };

        startBtn.setOnAction(evt -> {
            AgentManager.killAll();
            try {
                AgentManager.spawnAll(settings);
            } catch (StaleProxyException e) {
                e.printStackTrace();
                return;
            }
            simulationStartTime = System.currentTimeMillis();
            timer.start();
            startBtn.setText("Restart Simulation");
        });
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

            // Draw fire sensors
            if (id.startsWith("FireSensor")) {
                gc.setFill(Color.HOTPINK);
                double cx = pos[0]*cellWidth + cellWidth*0.25;
                double cy = pos[1]*cellHeight + cellHeight*0.25;
                gc.fillRect(cx, cy, cellWidth*0.4, cellWidth*0.4);
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
