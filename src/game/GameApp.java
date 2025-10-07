package game;

import static game.util.ResourceUtil.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Bounds;

import game.ui.MenuSonicGrafico;
import game.ui.HUD;
import game.state.SaveData;
import game.audio.SoundManager;
import game.entities.Player;
import game.world.CollisionManager;
import game.world.elements.FinishGate;
import game.levels.*;

public class GameApp extends Application implements MenuSonicGrafico.Listener {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
            showMenu();
        stage.setTitle("SonicX");
        stage.show();

        // load saved volumes
        SoundManager.setMaster(SaveData.getMaster());
        SoundManager.setMusic(SaveData.getMusic());
        SoundManager.setSfx(SaveData.getSfx());
    }

    private void showMenu() {
        // Ensure level music stops when returning to menu
        SoundManager.stopMusic();
        MenuSonicGrafico menu = new MenuSonicGrafico(this, 1280, 720);
        stage.setScene(menu.getScene());
    }

    @Override
    public void onStartLevelSelected(int index) {
        if (index == 2 && !SaveData.isLevelCompleted(1)) return;
        if (index == 3 && (!SaveData.isLevelCompleted(1) || !SaveData.isLevelCompleted(2))) return;
        runLevel(index);
    }

    @Override
    public void onExit() {
        stage.close();
    }

    private void runLevel(int index) {
        Group parallax = new Group();
        Group world = new Group();
        Group ui = new Group();
        Group root = new Group(parallax, world, ui);
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);

        // Camera follow (X) con smoothing; parallax si muove più lento
        final double[] camX = new double[]{0};
        final double CAMERA_LERP = 0.12;

        Player player = new Player(world, 60, 600);
        HUD hud = new HUD(ui);
        CollisionManager cm = new CollisionManager(world, player);
        player.setDamageContext(cm, world);

        Level level;
        if (index == 1)      level = new Level1();
        else if (index == 2) level = new Level2();
        else                 level = new Level3();

        // FIX: costruisce il livello dentro "world" (non "root")
        level.build(parallax, world, player, cm);

        // --- VITE & SPAWN ---
        final int[] lives = { game.Constants.PLAYER_LIVES };
        hud.setLives(lives[0]);

        double top = level.getGroundY();
        final double[] spawnX = {200}; // dentro la prima piattaforma (0..400)
        final double[] spawnY = {top - player.getSprite().getImage().getHeight()}; // appoggiato sul terreno

        // posizionamento iniziale
        player.getSprite().setTranslateX(spawnX[0]);
        player.getSprite().setTranslateY(spawnY[0]);
        player.setVelocityX(0);
        player.setVelocityY(0);

        // Snap collisioni
        cm.update(1.0 / 120.0);

        hud.setRings(player.getRings());
player.getSprite().toFront();

        // FIX: anche il gate sta in "world" per avere bounds coerenti con il player
        FinishGate gate = new FinishGate(world, level.getFinishX(), level.getGroundY());
        player.getSprite().toFront();

        // Input
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  e -> player.onKeyPressed(e.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> player.onKeyReleased(e.getCode()));

        AnimationTimer timer = new AnimationTimer() {
            private long last = -1;
            @Override
            public void handle(long now) {
                if (last < 0) last = now;
                double dt = (now - last) / 1_000_000_000.0;
                last = now;
                if (dt > game.Constants.MAX_DT) dt = game.Constants.MAX_DT;

                player.tick();
                cm.update(dt);

                if (player.consumeDeathQueued()) {
                    // Lose a life and respawn
                    lives[0]--;
                    if (lives[0] <= 0) { stop(); showMenu(); return; }
                    player.getSprite().setTranslateX(spawnX[0]);
                    player.getSprite().setTranslateY(spawnY[0]);
                    player.setVelocityX(0); player.setVelocityY(0);
                    cm.update(1.0/60.0);
                    camX[0] = Math.max(0, spawnX[0] - scene.getWidth() * 0.40);
                    world.setTranslateX(-camX[0]); parallax.setTranslateX(-camX[0] * 0.5);
                    hud.setLives(lives[0]);
                }

                hud.setRings(player.getRings());
// --- Camera follow & clamp ---
                double rawTarget = player.getSprite().getTranslateX() - scene.getWidth() * 0.40;
                double maxCam = Math.max(0, level.getFinishX() - scene.getWidth() * 0.90);
                double targetCamX = Math.max(0, Math.min(rawTarget, maxCam));
                camX[0] += (targetCamX - camX[0]) * CAMERA_LERP;
                world.setTranslateX(-camX[0]);
                parallax.setTranslateX(-camX[0] * 0.5);

                
// --- FALL CHECK: perde 1 vita e respawn, a 0 vite torna al menu ---
                if (player.getSprite().getTranslateY() > level.getGroundY() + 300) {
                    lives[0]--;
                    if (lives[0] <= 0) {
                        stop();
                        showMenu();
                        return;
                    }
                    // Respawn
                    player.getSprite().setTranslateX(spawnX[0]);
                    player.getSprite().setTranslateY(spawnY[0]);
                    player.setVelocityX(0);
                    player.setVelocityY(0);
                    cm.update(1.0 / 60.0);
                    hud.setRings(player.getRings());
camX[0] = Math.max(0, spawnX[0] - scene.getWidth() * 0.40);
                    world.setTranslateX(-camX[0]);
                    parallax.setTranslateX(-camX[0] * 0.5);
                    hud.setLives(lives[0]);
                }

                // --- CHECKPOINT a metà livello (disattivato nel Level 3) ---
                if (index != 3 && (player.getSprite().getTranslateX() >= level.getFinishX() * 0.5)) {
                    spawnX[0] = level.getFinishX() * 0.5;
                    spawnY[0] = level.getGroundY() - player.getSprite().getImage().getHeight();
                    hud.setMessage("Checkpoint!");
                }

                // HUD boss (solo Level3)
                if (index == 3) {
                    Level3 l3 = (Level3) level;
                    if (l3.getBoss() != null && !l3.getBoss().isDefeated()) {
                        hud.setBossStats(l3.getBoss().getHp(), l3.getBoss().toString());
                    } else {
                        hud.setMessage("BOSS DEFEATED!");
                    }
                }

                Bounds pb = player.getBounds();
                if (pb.intersects(gate.getBounds()) &&
                   (index != 3 || ((Level3) level).getBoss().isDefeated())) {
                    setLevelCompleted(index);
                    stop();
                    showMenu();
                }
            }
        };
        timer.start();
    }

    private void setLevelCompleted(int levelIndex) {
        SaveData.setLevelCompleted(levelIndex, true);
        System.out.println("Level " + levelIndex + " completed!");
    }
}
