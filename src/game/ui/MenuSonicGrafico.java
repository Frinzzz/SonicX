package game.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MenuSonicGrafico {

    public interface Listener {
        void onStartLevelSelected(int index);
        void onExit();
    }

    private final Scene scene;
    private final BorderPane root;

    public MenuSonicGrafico(Listener listener, double width, double height) {
        root = new BorderPane();
        scene = new Scene(root, width, height);

        VBox menu = new VBox(12);
        menu.setAlignment(Pos.CENTER);

        Button start = new Button("Start");
        Button exit  = new Button("Exit");

        start.setOnAction(e -> listener.onStartLevelSelected(1)); // placeholder: Level 1
        exit.setOnAction(e -> listener.onExit());

        menu.getChildren().addAll(start, exit);
        root.setCenter(menu);
    }

    public Scene getScene() { return scene; }
}

