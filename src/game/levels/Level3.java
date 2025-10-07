package game.levels;

import javafx.scene.Group;

import game.entities.Boss;
import game.entities.Player;
import game.world.CollisionManager;
import game.audio.SoundManager;
import game.world.Background;
import game.world.elements.Platform;
import game.world.elements.Spring;
import game.world.Ring;
import game.world.elements.*;

public class Level3 implements Level {

    private final double groundY = 520;
    private final double finishX = 5000;

    private Boss boss;
    public Boss getBoss() { return boss; }

    @Override
    public void build(Group parallax, Group root, Player player, CollisionManager cm) {
        
// === Background ===
// Tile the background across the level width (parallax layer)
int bgTiles = (int) Math.ceil(getFinishX() / 1280.0) + 2;
for (int i = 0; i < bgTiles; i++) {
    new Background(parallax, "file:src/game/assets/images/world/bg3.png", 1280, 720, i * 1280.0);
}
// play level-specific music
        SoundManager.playMusic("/assets/music/level3.mp3");


        // 3 piattaforme iniziali
        cm.addPlatform(new Platform(root, 200, groundY - 90, 140));

        // Pre-boss
        cm.addPlatform(new Platform(root, 300,  groundY - 40,  160));
        cm.addPlatform(new Platform(root, 520,  groundY - 80,  160));
        cm.addPlatform(new Platform(root, 760,  groundY - 60,  160));
        cm.addPlatform(new Platform(root, 980,  groundY - 120, 160));
        cm.addPlatform(new Platform(root, 1220, groundY - 160, 160));
        cm.addPlatform(new Platform(root, 1420, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 1620, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 1820, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 1920, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2020, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2120, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2220, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2320, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2420, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2520, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2620, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2720, groundY - 140, 160));
        cm.addPlatform(new Platform(root, 2820, groundY - 140, 160));


        // Boss con movimento ondulatorio
        boss = this.boss = new Boss(root, 2500, groundY - 120, cm);
        double periodX = 3.0, periodY = 2.2, phase = Math.PI / 2.0;

        boss.enableWaveMovement(2500, groundY - 160, 300, 50, periodX, periodY, phase);
        boss.enableShooting(); // oppure boss.enableShooting(1.0, 300, 0.09, 4);
        cm.addEnemy(boss);

    
}

    @Override public double getGroundY() { return groundY; }
    @Override public double getFinishX() { return finishX; }
}