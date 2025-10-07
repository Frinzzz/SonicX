package game.entities;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;

/** Simple projectile with linear velocity and auto-despawn on lifetime end. */
public class Projectile {

public Projectile(Group root, double x, double y, double vx, double vy) {
    this(root, x, y, vx, vy, 4.0);
}

    private final ImageView sprite;
    private double vx, vy;
    private double life; // seconds
    private boolean alive = true;

    public Projectile(Group root, double x, double y, double vx, double vy, double life) {
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        Image img = new Image("file:src/game/assets/images/entities/enemies/missile.gif", 16, 16, true, true, true);
        sprite = new ImageView(img);
        sprite.setTranslateX(x);
        sprite.setTranslateY(y);
        root.getChildren().add(sprite);
    }

    public void update(double dt) {
        if (!alive) return;
        life -= dt;
        if (life <= 0) { alive = false; return; }
        sprite.setTranslateX(sprite.getTranslateX() + vx * dt);
        sprite.setTranslateY(sprite.getTranslateY() + vy * dt);
        // apply slight gravity
        vy += 12 * dt;
    }

    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }
    public Bounds getBounds() { return sprite.getBoundsInParent(); }
    public ImageView getSprite() { return sprite; }
}
