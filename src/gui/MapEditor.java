package gui;

import environment.Cell;
import environment.MapIO;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class MapEditor extends Application {
    private String mapName;
    private Consumer<String> onSaved;
    private Cell.CellType[][] grid;
    private int cols, rows;
    private Canvas canvas;

    private int lastX = -1, lastY = -1;

    public MapEditor(String name) { this.mapName = name; }
    public void setOnSaved(Consumer<String> c){ this.onSaved = c; }

    @Override
    public void start(Stage stage) {
        if (MapIO.listSavedMapNames().contains(mapName)) {
            var env = MapIO.loadMap(mapName);
            cols = env.getWidth(); rows = env.getHeight();
            grid = new Cell.CellType[cols][rows];
            for (int x = 0; x < cols; x++)
                for (int y = 0; y < rows; y++)
                    grid[x][y] = env.getCell(x, y).getType();
        } else {
            cols = 60; rows = 45;
            grid = new Cell.CellType[cols][rows];
            for (int x = 0; x < cols; x++)
                for (int y = 0; y < rows; y++)
                    grid[x][y] = Cell.CellType.FREE;
        }

        canvas = new Canvas(900, 900.0 * rows / cols);
        drawMap();

        Spinner<Integer> wSpin = new Spinner<>(10,200,cols,5);
        Spinner<Integer> hSpin = new Spinner<>(10,200,rows,5);

        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> {
            cols = wSpin.getValue(); rows = hSpin.getValue();
            grid = new Cell.CellType[cols][rows];
            for (int x=0; x<cols; x++)
                for(int y=0; y<rows; y++)
                    grid[x][y] = Cell.CellType.FREE;
            resizeCanvas(stage);
            drawMap();
        });
        HBox sizePane = new HBox(10,
                new Label("Width:"), wSpin,
                new Label("Height:"), hSpin,
                newBtn
        );

        sizePane.setAlignment(Pos.CENTER);
        sizePane.setPadding(new Insets(5));

        ToggleGroup tg = new ToggleGroup();
        HBox pal = new HBox(10);
        pal.setAlignment(Pos.CENTER);
        for (Cell.CellType t : Cell.CellType.values()) {
            ToggleButton b = new ToggleButton(t.name());
            b.setUserData(t);
            b.setToggleGroup(tg);
            Rectangle icon = new Rectangle(12,12, switch(t){
                case FREE -> Color.WHITE;
                case WALL -> Color.DARKGRAY;
                case OBSTACLE -> Color.SADDLEBROWN;
                case EXIT -> Color.LIGHTGREEN;
            });
            b.setGraphic(icon);
            pal.getChildren().add(b);
            if (t == Cell.CellType.FREE)
                b.setSelected(true);
        }

        TextField nameField = new TextField(mapName);
        nameField.setPrefColumnCount(12);

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            try {
                MapIO.saveMap(nameField.getText().trim(), grid);
                if (onSaved != null) onSaved.accept(nameField.getText().trim());
                stage.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button delBtn = new Button("Delete");
        delBtn.setOnAction(e -> {
            try {
                MapIO.deleteMap(nameField.getText().trim());
                if (onSaved != null) onSaved.accept(null);
                stage.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox savePane = new HBox(10,
                new Label("Name:"), nameField,
                saveBtn, delBtn
        );
        savePane.setAlignment(Pos.CENTER);
        savePane.setPadding(new Insets(5));

        VBox root = new VBox(15, sizePane, pal, canvas, savePane);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10));

        canvas.setOnMousePressed(e -> {
            paintCell(e.getX(), e.getY(), e.getButton(), tg);
        });
        canvas.setOnMouseDragged(e -> {
            paintCell(e.getX(), e.getY(), e.getButton(), tg);
        });
        canvas.setOnMouseReleased(e -> {
            lastX = lastY = -1;
        });

        stage.widthProperty().addListener((o,old,n) -> resizeCanvas(stage));
        stage.heightProperty().addListener((o,old,n) -> resizeCanvas(stage));

        stage.setScene(new Scene(root, 1200, 900));
        stage.setTitle("Map Editor: " + mapName);
        stage.show();
        resizeCanvas(stage);
    }

    private void paintCell(double mx, double my, MouseButton mb, ToggleGroup tg) {
        int x = (int)(mx / (canvas.getWidth()/cols));
        int y = (int)(my / (canvas.getHeight()/rows));
        if (x<0||y<0||x>=cols||y>=rows) return;
        Cell.CellType ct = (Cell.CellType) tg.getSelectedToggle().getUserData();

        if (lastX >= 0) {
            int dx = Math.abs(x - lastX), sx = lastX < x ? 1 : -1;
            int dy = Math.abs(y - lastY), sy = lastY < y ? 1 : -1;
            int err = dx - dy;
            int cx = lastX, cy = lastY;
            while (true) {
                grid[cx][cy] = (mb==MouseButton.SECONDARY) ? Cell.CellType.FREE : ct;
                if (cx == x && cy == y) break;
                int e2 = 2 * err;
                if (e2 > -dy) { err -= dy; cx += sx; }
                if (e2 < dx)  { err += dx; cy += sy; }
            }
        } else {
            grid[x][y] = (mb==MouseButton.SECONDARY) ? Cell.CellType.FREE : ct;
        }

        lastX = x;
        lastY = y;
        drawMap();
    }

    private void resizeCanvas(Stage s) {
        double w = s.getWidth() - 40, h = s.getHeight() - 200;
        double tile = Math.min(w/cols, h/rows);
        canvas.setWidth(tile*cols);
        canvas.setHeight(tile*rows);
        drawMap();
    }

    private void drawMap() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cw = canvas.getWidth()/cols, ch = canvas.getHeight()/rows;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                switch (grid[x][y]) {
                    case FREE -> gc.setFill(Color.WHITE);
                    case WALL -> gc.setFill(Color.DARKGRAY);
                    case OBSTACLE -> gc.setFill(Color.SADDLEBROWN);
                    case EXIT -> gc.setFill(Color.LIGHTGREEN);
                }
                gc.fillRect(x*cw, y*ch, cw, ch);
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeRect(x*cw, y*ch, cw, ch);
            }
        }
    }
}
