package game.entities;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WalkerEnemy extends EnemyBase {
    private double speed = 1.2;

    public WalkerEnemy(Group root, double x, double y) {
        Image img = new Image("file:src/game/assets/images/entities/enemies/walker.gif", 40, 40, true, true, true);
        sprite = new ImageView(img);
        sprite.setTranslateX(x);
        sprite.setTranslateY(y);
        root.getChildren().add(sprite);
    }

    @Override
    public void update(double dt) {
        if (!alive) return;
        sprite.setTranslateX(sprite.getTranslateX() + Math.sin(System.nanoTime()*1e-9)*speed);
    }
}
