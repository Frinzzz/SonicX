package game.world;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import game.entities.Shield;

/** Semplice pickup di scudo. Statico, con sprite. */
public class ShieldPickup {
    private final ImageView sprite;
    private final Shield.Type type;
    private boolean collected = false;

    public ShieldPickup(Group root, double x, double y, Shield.Type type) {
        this.type = (type == null ? Shield.Type.NORMAL : type);
        // riuso dell'asset dello scudo
        Image img = safeImage(
            "file:src/game/assets/images/entities/player/ShieldStH216Bit.gif"
        );
        sprite = new ImageView(img);
        sprite.setFitWidth(44);
        sprite.setFitHeight(44);
        sprite.setTranslateX(x - 22);
        sprite.setTranslateY(y - 22);
        sprite.setSmooth(true);
        root.getChildren().add(sprite);
    }

    public javafx.geometry.Bounds getBounds() { return sprite.getBoundsInParent(); }
    public ImageView getSprite() { return sprite; }
    public Shield.Type getType() { return type; }
    public boolean isCollected() { return collected; }
    public void collect() { collected = true; if (sprite.getParent() instanceof Group g) g.getChildren().remove(sprite); }

    private static Image safeImage(String... candidates) {
        for (String u : candidates) {
            try { return new Image(u); } catch (Throwable ignored) {}
        }
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9lZ7p1EAAAAASUVORK5CYII=");
    }
}
