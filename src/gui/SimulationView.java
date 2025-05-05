package gui;

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

    public static void setEnvironment(GridEnvironment environment) {
        env = environment;
    }

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(1200, 900);
        StackPane root = new StackPane(canvas);
        stage.setScene(new Scene(root));
        stage.setTitle("Evacuation Simulation");
        stage.show();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
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

            switch (env.getAgentType(id)) {
                case CALM -> gc.setFill(Color.BLUE);
                case PANICKED -> gc.setFill(Color.ORANGE);
                case INJURED -> gc.setFill(Color.RED);
            }

            double cx = pos[0]*cellWidth + cellWidth*0.25;
            double cy = pos[1]*cellHeight + cellHeight*0.25;
            gc.fillOval(cx, cy, cellWidth*0.5, cellHeight*0.5);
        }
    }
}
