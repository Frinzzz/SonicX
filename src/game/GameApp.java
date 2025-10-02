package game;

import javafx.application.Application;
import javafx.stage.Stage;
import game.ui.MenuSonicGrafico;
import game.state.SaveData;
import game.audio.SoundManager;

public class GameApp extends Application implements MenuSonicGrafico.Listener {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        showMenu();
        stage.setTitle("Sonic Menu + Level Demo");
        stage.show();

        SoundManager.setMaster(SaveData.getMaster());
        SoundManager.setMusic(SaveData.getMusic());
        SoundManager.setSfx(SaveData.getSfx());
    }

    private void showMenu() {
        SoundManager.stopMusic();
        MenuSonicGrafico menu = new MenuSonicGrafico(this, 1280, 720);
        stage.setScene(menu.getScene());
    }

    @Override
    public void onStartLevelSelected(int index) {
        // Solo gating
        if (index == 2 && !SaveData.isLevelCompleted(1)) return;
        if (index == 3 && (!SaveData.isLevelCompleted(1) || !SaveData.isLevelCompleted(2))) return;
        showMenu();
    }

    @Override
    public void onExit() {
        stage.close();
    }
}
