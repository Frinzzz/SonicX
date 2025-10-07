package game.world.elements;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Bounds;

/** Solid obstacle that hurts the player. */
public class Obstacle {
    private final Rectangle rect;

    public Obstacle(Group root, double x, double y, double w, double h) {
        rect = new Rectangle(w, h);
        rect.setTranslateX(x);
        rect.setTranslateY(y);
        try {
            Image tex = new Image("file:src/game/assets/images/world/obstacle.png");
            rect.setFill(new ImagePattern(tex, 0, 0, tex.getWidth(), tex.getHeight(), false));
        } catch (Throwable t) {
            rect.setFill(Color.DARKRED);
        }
        root.getChildren().add(rect);
    }

    public Bounds getBounds() { return rect.getBoundsInParent(); }
}