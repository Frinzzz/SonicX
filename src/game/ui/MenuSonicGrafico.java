package game.ui;

import static game.util.ResourceUtil.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    final Scene scene;
    final BorderPane root;
    final Listener listener;

    public MenuSonicGrafico(Listener listener, double width, double height) {
        this.listener = listener;
        root = new BorderPane();
        scene = new Scene(root, width, height);

        StackPane center = new StackPane(new ImageView(image("/assets/images/ui/background.gif")));
        root.setCenter(center);

        ImageView logo = new ImageView(image("/assets/images/ui/logo.png"));
        logo.setPreserveRatio(true);
        logo.setFitHeight(140);
        StackPane top = new StackPane(logo);
        top.setPrefHeight(160);
        root.setTop(top);

        VBox menuBox = new VBox(16);
        menuBox.setAlignment(Pos.CENTER);
        Button start = new Button("Start");
        Button options = new Button("Options");
        Button exit = new Button("Exit");
        menuBox.getChildren().addAll(start, options, exit);
        center.getChildren().add(menuBox);

        // Volumi + musica
        SoundManager.setMaster(SaveData.getMaster());
        SoundManager.setMusic(SaveData.getMusic());
        SoundManager.setSfx(SaveData.getSfx());
        SoundManager.playMusic("/assets/music/menu.mp3");

        start.setOnAction(e -> root.setCenter(buildSelectLevel(scene.getWidth(), scene.getHeight())));
        options.setOnAction(e -> root.setCenter(buildOptions(scene.getWidth(), scene.getHeight())));
        exit.setOnAction(e -> listener.onExit());
    }

    private StackPane buildSelectLevel(double width, double height) {
        ImageView bg = new ImageView(image("/assets/images/ui/background.gif"));
        bg.setFitWidth(width);
        bg.setFitHeight(height);
        bg.setPreserveRatio(false);
        StackPane pane = new StackPane(bg);

        VBox col = new VBox(12);
        col.setAlignment(Pos.CENTER);

        Text title = new Text("Select Level");
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        Button l1 = levelButton(1, true);
        boolean l2Unlocked = SaveData.isLevelCompleted(1);
        boolean l3Unlocked = l2Unlocked && SaveData.isLevelCompleted(2);

        Button l2 = levelButton(2, l2Unlocked);
        Button l3 = levelButton(3, l3Unlocked);

        row.getChildren().addAll(l1, l2, l3);

        Button back = new Button("Back");
        back.setOnAction(e -> {
            MenuSonicGrafico fresh = new MenuSonicGrafico(listener, width, height);
            scene.setRoot(fresh.root);
        });

        col.getChildren().addAll(title, row, back);
        pane.getChildren().add(col);
        return pane;
    }

    private Button levelButton(int index, boolean unlocked) {
        Button b = new Button(unlocked ? ("Level " + index) : ("Level " + index + " (LOCKED)"));
        b.setDisable(!unlocked);
        b.setOnAction(e -> {
            if (!b.isDisable()) listener.onStartLevelSelected(index);
        });
        return b;
    }

    private StackPane buildOptions(double width, double height) {
        ImageView bg = new ImageView(image("/assets/images/ui/background.gif"));
        bg.setFitWidth(width);
        bg.setFitHeight(height);
        bg.setPreserveRatio(false);
        StackPane pane = new StackPane(bg);

        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);

        box.getChildren().add(optionRow("Master",
            SaveData.getMaster(),
            v -> { SaveData.setMaster(v); SoundManager.setMaster(v); }));

        box.getChildren().add(optionRow("Music",
            SaveData.getMusic(),
            v -> { SaveData.setMusic(v); SoundManager.setMusic(v); }));

        box.getChildren().add(optionRow("SFX",
            SaveData.getSfx(),
            v -> { SaveData.setSfx(v); SoundManager.setSfx(v); }));

        Button back = new Button("Back");
        back.setOnAction(e -> {
            MenuSonicGrafico fresh = new MenuSonicGrafico(listener, width, height);
            scene.setRoot(fresh.root);
        });

        box.getChildren().add(back);
        pane.getChildren().add(box);
        return pane;
    }

    private HBox optionRow(String label, double initial, java.util.function.DoubleConsumer onChange) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER);
        javafx.scene.text.Text t = new javafx.scene.text.Text(label);
        javafx.scene.control.Slider s = new javafx.scene.control.Slider(0, 1, initial);
        s.setShowTickLabels(true);
        s.setShowTickMarks(true);
        s.valueProperty().addListener((obs, ov, nv) -> onChange.accept(nv.doubleValue()));
        r.getChildren().addAll(t, s);
        return r;
    }

    public Scene getScene() { return scene; }
}
