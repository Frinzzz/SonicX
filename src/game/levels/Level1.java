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

public class Level1 implements Level {
    private final double groundY = 520;
    private final double finishX = 5350;

    @Override
    public void build(Group parallax, Group root, Player player, CollisionManager cm) {
        
// === Background ===
// Tile the background across the level width (parallax layer)
int bgTiles = (int) Math.ceil(getFinishX() / 1280.0) + 2;
for (int i = 0; i < bgTiles; i++) {
    new Background(parallax, "file:src/game/assets/images/world/bg.png", 1280, 720, i * 1280.0);
}
// play level-specific music
        SoundManager.playMusic("/assets/music/level1.mp3");

        // Background moved to parallax layer in GameApp
// Basic ground segments as platforms
        cm.addPlatform(new Platform(root, 0, groundY, 400));
        cm.addRing(new Ring(root, 276, groundY - 23));
        cm.addRing(new Ring(root, 320, groundY - 23));
        cm.addPlatform(new Platform(root, 400, groundY + 80, 300));

        cm.addEnemy(new WalkerEnemy(root, 450, groundY + 35));
        cm.addEnemy(new ShooterEnemy(root, 500, groundY + 35, cm));

        cm.addMovingPlatform(new MovingPlatform(root, 446, groundY, 120, MovingPlatform.Axis.HORIZONTAL, 50, 1.5));

        cm.addPlatform(new Platform(root, 668, groundY, 800));
        cm.addRing(new Ring(root, 750, groundY - 23));
        cm.addRing(new Ring(root, 800, groundY - 23));
        cm.addRing(new Ring(root, 850, groundY - 23));
        cm.addLoop(new LoopDeLoop(root, 950, groundY - 60, 60));
        cm.addObstacle(new Obstacle(root, 1230, groundY - 45, 32, 40));

        cm.addMovingPlatform(new MovingPlatform(root, 1528, groundY, 120, MovingPlatform.Axis.VERTICAL, 90, 1.5));

        cm.addPlatform(new Platform(root, 1706, groundY + 90, 300));
        cm.addPlatform(new Platform(root, 1706, groundY - 90, 300));
        cm.addObstacle(new Obstacle(root, 1817, groundY - 135, 32, 40));
        cm.addPlatform(new Platform(root, 2106, groundY + 90, 300));
        cm.addPlatform(new Platform(root, 2106, groundY - 90, 300));
        cm.addObstacle(new Obstacle(root, 2217, groundY - 135, 32, 40));
        cm.addPlatform(new Platform(root, 2462, groundY, 250));
        cm.addPlatform(new Platform(root, 2818, groundY - 45, 250));
        cm.addObstacle(new Obstacle(root, 2929, groundY - 90, 32, 40));
        cm.addPlatform(new Platform(root, 3100, groundY - 135, 800));
        cm.addLoop(new LoopDeLoop(root, 3382, groundY - 195, 60));
        cm.addEnemy(new WalkerEnemy(root, 3650, groundY - 180));

        cm.addMovingPlatform(new MovingPlatform(root, 4093, groundY, 100, MovingPlatform.Axis.HORIZONTAL, 177, 2.5));
        cm.addMovingPlatform(new MovingPlatform(root, 4410, groundY - 67, 100, MovingPlatform.Axis.VERTICAL, 67, 3));

        cm.addPlatform(new Platform(root, 4505, groundY - 134, 550));
        cm.addPlatform(new Platform(root, 5083, groundY - 89, 150));
        cm.addPlatform(new Platform(root, 5200, groundY, 150));
        


        // Moving platform
        

        // Springs

        // Obstacles


        
        
        
        
        // --- Shield pickup (Level1) ---
        cm.addShieldPickup(new game.world.ShieldPickup(root, 520, groundY - 80, game.entities.Shield.Type.NORMAL));
       
    
}

    @Override public double getGroundY() { return groundY; }
    @Override public double getFinishX() { return finishX; }
}