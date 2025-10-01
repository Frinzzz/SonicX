package game.ui;

import static game.util.ResourceUtil.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import game.state.SaveData;
import game.audio.SoundManager;

public class MenuSonicGrafico {

    public interface Listener {
        void onStartLevelSelected(int index);
        void onExit();
    }

    private final Scene scene;
    private final BorderPane root;
    private final Listener listener;

    public MenuSonicGrafico(Listener listener, double width, double height) {
        this.listener = listener;
        root = new BorderPane();
        scene = new Scene(root, width, height);

        ImageView bg = new ImageView(image("/assets/images/ui/background.gif"));
        bg.setFitWidth(width);
        bg.setFitHeight(height);
        bg.setPreserveRatio(false);
        StackPane center = new StackPane(bg);
        root.setCenter(center);

        ImageView logo = new ImageView(image("/assets/images/ui/logo.png"));
        logo.setPreserveRatio(true);
        logo.setFitHeight(140);
        StackPane top = new StackPane(logo);
        top.setPrefHeight(160);
        root.setTop(top);

        // Pannello principale (Start/Options/Exit)
        VBox menuBox = new VBox(16);
        menuBox.setAlignment(Pos.CENTER);
        Button start = new Button("Start");
        Button options = new Button("Options");
        Button exit = new Button("Exit");
        menuBox.getChildren().addAll(start, options, exit);
        center.getChildren().add(menuBox);

        // Musica del menu
        SoundManager.setMaster(SaveData.getMaster());
        SoundManager.setMusic(SaveData.getMusic());
        SoundManager.setSfx(SaveData.getSfx());
        SoundManager.playMusic("/assets/music/menu.mp3");

        // Navigazione
        start.setOnAction(e -> listener.onStartLevelSelected(1));
        exit.setOnAction(e -> listener.onExit());
        options.setOnAction(e -> root.setCenter(buildOptions(width, height)));
    }

    private StackPane buildOptions(double width, double height) {
        ImageView bg = new ImageView(image("/assets/images/ui/background.gif"));
        bg.setFitWidth(width);
        bg.setFitHeight(height);
        bg.setPreserveRatio(false);
        StackPane pane = new StackPane(bg);

        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);

        HBox rowMaster = sliderRow("Master", SaveData.getMaster(), v -> {
            SaveData.setMaster(v);
            SoundManager.setMaster(v);
        });
        HBox rowMusic = sliderRow("Music", SaveData.getMusic(), v -> {
            SaveData.setMusic(v);
            SoundManager.setMusic(v);
        });
        HBox rowSfx = sliderRow("SFX", SaveData.getSfx(), v -> {
            SaveData.setSfx(v);
            SoundManager.setSfx(v);
        });

        Button back = new Button("Back");
        back.setOnAction(e -> {
            // torna al menu principale
            root.setCenter(new StackPane(new ImageView(image("/assets/images/ui/background.gif"))));
            // ricrea il pannello principale
            MenuSonicGrafico fresh = new MenuSonicGrafico(listener, width, height);
            scene.setRoot(fresh.root);
        });

        box.getChildren().addAll(rowMaster, rowMusic, rowSfx, back);
        pane.getChildren().add(box);
        return pane;
    }

    private HBox sliderRow(String label, double initial, java.util.function.DoubleConsumer onChange) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER);
        Text t = new Text(label);
        Slider s = new Slider(0, 1, initial);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.valueProperty().addListener((obs, ov, nv) -> onChange.accept(nv.doubleValue()));
        r.getChildren().addAll(t, s);
        return r;
    }

    public Scene getScene() { return scene; }
}
