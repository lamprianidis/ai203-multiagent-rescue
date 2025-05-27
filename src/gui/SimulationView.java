package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import environment.MapIO;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import agents.EvacueeAgent;
import agents.manager.AgentManager;
import agents.manager.AgentSettings;
import environment.Cell;
import environment.EnvironmentFactory;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import jade.wrapper.StaleProxyException;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.io.OutputStream;
import java.io.PrintStream;

public class SimulationView extends Application {
    private static GridEnvironment env;
    private static AgentSettings settings;

    private Canvas canvas;
    private long simulationStartTime;
    private AnimationTimer timer;
    private static final long maxDuration = 60_000; // 30 sec

    public static void setEnvironment(GridEnvironment environment) {
        env = environment;
    }

    public static void setAgentSettings(AgentSettings agentSettings) {
        settings = agentSettings;
    }

    private enum ShapeType { CALM, PANICKED, INJURED, FIREFIGHTER, RESCUER, FIRESENSOR, FIREPLACE }

    private Node makeIcon(ShapeType type) {
        switch (type) {
            case CALM:
                return new Circle(6, Color.BLUE);
            case PANICKED:
                return new Circle(6, Color.ORANGE);
            case INJURED:
                return new Circle(6, Color.RED);
            case FIREFIGHTER:
                Polygon tri = new Polygon(0.0, 8.0, 8.0, 8.0, 4.0, 0.0);
                tri.setFill(Color.GOLD);
                return tri;
            case RESCUER:
                Canvas icon = new Canvas(12, 12);
                GraphicsContext igc = icon.getGraphicsContext2D();
                igc.setStroke(Color.GREEN);
                igc.setLineWidth(2);
                double cx = icon.getWidth() * 0.5;
                double cy = icon.getHeight() * 0.5;
                double size = Math.min(icon.getWidth(), icon.getHeight()) * 0.3;
                igc.strokeLine(cx - size, cy, cx + size, cy);
                igc.strokeLine(cx, cy - size, cx, cy + size);
                return icon;
            case FIRESENSOR:
                return new Rectangle(12, 12, Color.HOTPINK);
            case FIREPLACE:
                return new Rectangle(12, 12, Color.DARKRED);
            default:
                return new Rectangle(0, 0);
        }
    }

    private Spinner<Integer> makeSpinner(int initial) {
        Spinner<Integer> sp = new Spinner<>();
        sp.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 400, initial, 1)
        );
        sp.setEditable(true);
        sp.setPrefWidth(60);
        sp.getEditor().setPrefColumnCount(3);
        return sp;
    }

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(1000, 750);

        Label mapLabel = new Label("Map");
        mapLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        ObservableList<String> mapNames = FXCollections.observableArrayList(MapIO.listSavedMapNames());
        ComboBox<String> mapCombo = new ComboBox<>(mapNames);
        mapCombo.getSelectionModel().selectFirst();

        Button loadMapBtn = new Button("Load");
        Button editMapBtn = new Button("Edit");
        HBox mapButtons = new HBox(5, loadMapBtn, editMapBtn);
        mapButtons.setAlignment(Pos.CENTER_LEFT);
        VBox mapBox = new VBox(5, mapLabel, mapCombo, mapButtons);
        mapBox.setPadding(new Insets(5));

        GridEnvironment defaultEnv = EnvironmentFactory.buildOfficeEnvironment();
        EnvironmentHolder.setEnvironment(defaultEnv);
        SimulationView.setEnvironment(defaultEnv);
        drawGrid();
        drawAgents();

        Label agentsLabel = new Label("Agents");
        agentsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Spinner<Integer> calmSp = makeSpinner(settings.calmCount);
        Label calmLbl = new Label("Calm evacuees:");
        calmLbl.setGraphic(makeIcon(ShapeType.CALM));
        calmLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox calmRow = new HBox(8, calmLbl, calmSp);
        calmRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> panickedSp = makeSpinner(settings.panickedCount);
        Label panickedLbl = new Label("Panicked evacuees:");
        panickedLbl.setGraphic(makeIcon(ShapeType.PANICKED));
        panickedLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox panickedRow = new HBox(8, panickedLbl, panickedSp);
        panickedRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> injuredSp = makeSpinner(settings.injuredCount);
        Label injuredLbl = new Label("Injured evacuees:");
        injuredLbl.setGraphic(makeIcon(ShapeType.INJURED));
        injuredLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox injuredRow = new HBox(8, injuredLbl, injuredSp);
        injuredRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> firefighterSp = makeSpinner(settings.firefighterCount);
        Label firefighterLbl = new Label("Firefighters:");
        firefighterLbl.setGraphic(makeIcon(ShapeType.FIREFIGHTER));
        firefighterLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox firefighterRow = new HBox(8, firefighterLbl, firefighterSp);
        firefighterRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> rescuerSp = makeSpinner(settings.rescuerCount);
        Label rescuerLbl = new Label("Rescuers:");
        rescuerLbl.setGraphic(makeIcon(ShapeType.RESCUER));
        rescuerLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox rescuerRow = new HBox(8, rescuerLbl, rescuerSp);
        rescuerRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> sensorSp = makeSpinner(settings.fireSensorCount);
        Label sensorLbl = new Label("Fire sensors:");
        sensorLbl.setGraphic(makeIcon(ShapeType.FIRESENSOR));
        sensorLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox sensorRow = new HBox(8, sensorLbl, sensorSp);
        sensorRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> fireSp = makeSpinner(settings.fireplaceCount);
        Label fireLbl = new Label("Fireplaces:");
        fireLbl.setGraphic(makeIcon(ShapeType.FIREPLACE));
        fireLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox fireRow = new HBox(8, fireLbl, fireSp);
        fireRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> severitySp = makeSpinner(settings.fireSeverity);
        Label severityLbl = new Label("Fire severity:");
        severityLbl.setGraphic(makeIcon(ShapeType.FIREPLACE));
        severityLbl.setContentDisplay(ContentDisplay.LEFT);
        HBox severityRow = new HBox(8, severityLbl, severitySp);
        severityRow.setAlignment(Pos.CENTER_LEFT);

        Label evacuatedLabel = new Label("Evacuated");
        evacuatedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Text calmLabelText = new Text("Calm: ");
        calmLabelText.setFill(Color.BLUE);
        Text calmCountText = new Text("0 / " + settings.calmCount);
        TextFlow calmFlow   = new TextFlow(calmLabelText, calmCountText);

        Text panickedLabelText = new Text("Panicked: ");
        panickedLabelText.setFill(Color.ORANGE);
        Text panickedCountText = new Text("0 / " + settings.panickedCount);
        TextFlow panickedFlow   = new TextFlow(panickedLabelText, panickedCountText);

        Text injuredLabelText = new Text("Injured: ");
        injuredLabelText.setFill(Color.RED);
        Text injuredCountText = new Text("0 / " + settings.injuredCount);
        TextFlow injuredFlow   = new TextFlow(injuredLabelText, injuredCountText);

        Text deathLabel = new Text("Deaths: ");
        deathLabel.setFill(Color.BLACK);
        Text deathCountText = new Text("0");
        TextFlow deathFlow = new TextFlow(deathLabel, deathCountText);

        Button startBtn = new Button("Start Simulation");
        VBox leftPane = new VBox(15,
                mapBox,
                agentsLabel,
                calmRow,
                panickedRow,
                injuredRow,
                firefighterRow,
                rescuerRow,
                sensorRow,
                fireRow,
                severityRow,
                startBtn,
                evacuatedLabel,
                calmFlow,
                panickedFlow,
                injuredFlow,
                deathFlow
        );
        leftPane.setPadding(new Insets(10));

        TextArea consoleArea = getConsoleArea();

        BorderPane.setMargin(consoleArea, new Insets(5));

        PrintStream ps = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> consoleArea.appendText(String.valueOf((char) b)));
            }
        });
        System.setOut(ps);
        System.setErr(ps);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setLeft(leftPane);
        root.setBottom(consoleArea);

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

                calmCountText.setText(
                        env.getEvacuatedCount(EvacueeAgent.Type.CALM) + " / " + settings.calmCount
                );
                panickedCountText.setText(
                        env.getEvacuatedCount(EvacueeAgent.Type.PANICKED) + " / " + settings.panickedCount
                );
                injuredCountText.setText(
                        env.getEvacuatedCount(EvacueeAgent.Type.INJURED) + " / " + settings.injuredCount
                );
                deathCountText.setText(String.valueOf(EvacueeAgent.getTotalDeaths()));

                drawGrid();
                drawAgents();
            }
        };

        startBtn.setOnAction(evt -> {
            settings.calmCount = calmSp.getValue();
            settings.panickedCount = panickedSp.getValue();
            settings.injuredCount = injuredSp.getValue();
            settings.firefighterCount = firefighterSp.getValue();
            settings.rescuerCount = rescuerSp.getValue();
            settings.fireSensorCount = sensorSp.getValue();
            settings.fireplaceCount = fireSp.getValue();
            settings.fireSeverity = severitySp.getValue();

            EvacueeAgent.resetTotalDeaths();

            String selected = mapCombo.getValue();
            if (selected == null) return;

            GridEnvironment newEnv = MapIO.loadMap(selected);
            EnvironmentHolder.setEnvironment(newEnv);
            SimulationView.setEnvironment(newEnv);

            AgentManager.killAll();
            try {
                AgentManager.spawnAll(settings);
            } catch (StaleProxyException e) {
                e.printStackTrace();
                return;
            }

            simulationStartTime = System.currentTimeMillis();
            timer.stop();
            timer.start();

            startBtn.setText("Restart Simulation");
        });

        loadMapBtn.setOnAction(evt -> {
            timer.stop();
            startBtn.setText("Start Simulation");

            AgentManager.killAll();

            String selected = mapCombo.getValue();
            if (selected == null) return;

            GridEnvironment newEnv = MapIO.loadMap(selected);
            EnvironmentHolder.setEnvironment(newEnv);
            SimulationView.setEnvironment(newEnv);

            drawGrid();
            drawAgents();

            simulationStartTime = System.currentTimeMillis();
        });

        editMapBtn.setOnAction(evt -> {
            String selected = mapCombo.getValue();
            MapEditor editor = new MapEditor(selected);
            editor.setOnSaved(name -> {
                mapNames.setAll(MapIO.listSavedMapNames());
                mapCombo.getSelectionModel().select(name);
            });
            try {
                editor.start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private static TextArea getConsoleArea() {
        TextArea consoleArea = new TextArea();
        consoleArea.setWrapText(true);
        consoleArea.setPrefRowCount(8);
        consoleArea.setPrefHeight(180);
        consoleArea.setStyle("""
            -fx-border-color: #444444;
            -fx-border-width: 1;
            -fx-background-color: black;
            -fx-control-inner-background: #2B2B2B;
            -fx-text-fill: #D4D4D4;;
            -fx-font-family: 'Consolas', monospace;
            -fx-font-size: 13;
        """);
        consoleArea.setText("Simulation Console\n\n[ Press “Start Simulation” to begin... ]");
        return consoleArea;
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
