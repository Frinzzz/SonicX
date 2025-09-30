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

