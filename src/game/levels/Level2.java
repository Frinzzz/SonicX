package game.levels;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import game.entities.Player;
import game.world.CollisionManager;
import game.audio.SoundManager;
import game.world.Background;
import game.world.elements.*;
import game.world.Ring;
import game.entities.WalkerEnemy;
import game.entities.ShooterEnemy;
import javafx.scene.paint.Color;

/** Secondo livello: più lungo, più piattaforme mobili, qualche ostacolo in più. */
public class Level2 implements Level {
    private final double groundY = 540;
    private final double finishX = 4600;

    @Override
    public void build(Group parallax, Group root, Player player, CollisionManager cm) {
        
// === Background ===
// Tile the background across the level width (parallax layer)
int bgTiles = (int) Math.ceil(getFinishX() / 1280.0) + 2;
for (int i = 0; i < bgTiles; i++) {
    new Background(parallax, "file:src/game/assets/images/world/bg2.png", 1280, 720, i * 1280.0);
}
// play level-specific music
        SoundManager.playMusic("/assets/music/level2.mp3");

        // Background moved to parallax layer in GameApp
// Terreno a terrazze
        cm.addPlatform(new Platform(root, 0, groundY, 800));
        cm.addPlatform(new Platform(root, 1000, groundY + 80, 200));
        cm.addPlatform(new Platform(root, 1000, groundY - 40, 400));
        cm.addPlatform(new Platform(root, 1275, groundY + 80, 100));
        cm.addMovingPlatform(new MovingPlatform(root, 1500, groundY, 120, MovingPlatform.Axis.HORIZONTAL, 100, 1.5));
        cm.addPlatform(new Platform(root, 1850, groundY, 600));
        cm.addSpring(new Spring(root, 2411,  groundY - 22, 1100));
        cm.addLoop(new LoopDeLoop(root, 2150, groundY - 60, 60));
        cm.addPlatform(new Platform(root, 2495, groundY - 267, 300));
        cm.addPlatform(new Platform(root, 2495, groundY, 300));
        cm.addPlatform(new Platform(root, 2820, groundY, 300));
        cm.addObstacle(new Obstacle(root, 2865, groundY - 45, 32, 40));
        cm.addObstacle(new Obstacle(root, 3087, groundY - 45, 32, 40));
        cm.addMovingPlatform(new MovingPlatform(root, 3287, groundY, 120, MovingPlatform.Axis.HORIZONTAL, 80, 1.5));
        cm.addMovingPlatform(new MovingPlatform(root, 3598, groundY, 120, MovingPlatform.Axis.VERTICAL, 80, 1.5));
        cm.addPlatform(new Platform(root, 3828, groundY + 80, 300));
        cm.addSpring(new Spring(root, 4100,  groundY + 58, 1100));
        cm.addPlatform(new Platform(root, 4272, groundY - 50, 300));
        cm.addPlatform(new Platform(root, 4716, groundY + 80, 300));
        cm.addSpring(new Spring(root, 4988,  groundY + 58, 1100));
        cm.addPlatform(new Platform(root, 4272, groundY - 50, 300));
    
    
}

    @Override public double getGroundY() { return groundY; }
    @Override public double getFinishX() { return finishX; }
}