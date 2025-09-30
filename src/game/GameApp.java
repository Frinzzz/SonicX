package game;

import javafx.application.Application;
import javafx.stage.Stage;
import game.ui.MenuSonicGrafico;

public class GameApp extends Application implements MenuSonicGrafico.Listener {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        showMenu();
        stage.setTitle("Sonic Menu + Level Demo");
        stage.show();
    }

    private void showMenu() {
        MenuSonicGrafico menu = new MenuSonicGrafico(this, 1280, 720);
        stage.setScene(menu.getScene());
    }

    @Override
    public void onStartLevelSelected(int index) {
        // placeholder: torniamo al menu finché non esistono i livelli
        showMenu();
    }

    @Override
    public void onExit() {
        stage.close();
    }
}
