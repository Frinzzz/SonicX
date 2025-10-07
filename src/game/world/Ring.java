package game.world;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;

public class Ring {
    private final ImageView sprite;
    private boolean collected = false;
    private double vx = 0, vy = 0;
    private double ttl = Double.POSITIVE_INFINITY;
    private double pickupDelay = 0.0;
    private static final double PICKUP_DELAY_ON_DROP = 0.5;

    
public Ring(Group root, double x, double y, double vx, double vy, double ttlSeconds) {
    Image img = new Image("file:src/game/assets/images/world/ring.gif", 24, 24, true, true, true);
    sprite = new ImageView(img);
    sprite.setTranslateX(x);
    sprite.setTranslateY(y);
    this.vx = vx;
    this.vy = vy;
    this.ttl = ttlSeconds;
    this.pickupDelay = PICKUP_DELAY_ON_DROP;
    root.getChildren().add(sprite);
}

    public Ring(Group root, double x, double y) {
        Image img = new Image("file:src/game/assets/images/world/ring.gif", 24, 24, true, true, true);
        sprite = new ImageView(img);
        sprite.setTranslateX(x);
        sprite.setTranslateY(y);
        root.getChildren().add(sprite);
    }

    public boolean isCollected() { return collected; }
    public void collect() { collected = true; sprite.setVisible(false); }
    public Bounds getBounds() { return sprite.getBoundsInParent(); }
    public ImageView getSprite() { return sprite; }
    public void update(double dt) {
        if (collected) return;
        if (ttl == Double.POSITIVE_INFINITY) return; // static level ring, no physics
        // simple physics: gravity + integrate
        vy += 900 * dt;
        sprite.setTranslateX(sprite.getTranslateX() + vx * dt);
        sprite.setTranslateY(sprite.getTranslateY() + vy * dt);
        if (ttl != Double.POSITIVE_INFINITY) ttl -= dt;
        if (pickupDelay > 0) pickupDelay -= dt;
    }
    public boolean isExpired() { return ttl != Double.POSITIVE_INFINITY && ttl <= 0; }
    public boolean canBeCollected() { return !collected && pickupDelay <= 0; }
    public double getX() { return sprite.getTranslateX(); }
    public double getY() { return sprite.getTranslateY(); }
    public void setX(double x) { sprite.setTranslateX(x); }
    public void setY(double y) { sprite.setTranslateY(y); }
    public double getVX() { return vx; }
    public double getVY() { return vy; }
    public void setVX(double v) { vx = v; }
    public void setVY(double v) { vy = v; }
    public boolean isDynamic() { return ttl != Double.POSITIVE_INFINITY && !collected; }
}
