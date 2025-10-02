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

        // carica volumi salvati
        SoundManager.setMaster(SaveData.getMaster());
        SoundManager.setMusic(SaveData.getMusic());
        SoundManager.setSfx(SaveData.getSfx());
    }

    private void showMenu() {
        // stop musica livello quando rientri nel menu
        SoundManager.stopMusic();
        MenuSonicGrafico menu = new MenuSonicGrafico(this, 1280, 720);
        stage.setScene(menu.getScene());
    }

    @Override
    public void onStartLevelSelected(int index) {
        showMenu(); // ancora nessun livello
    }

    @Override
    public void onExit() {
        stage.close();
    }
}
