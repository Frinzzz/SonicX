package game.world.elements;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/** Axis-aligned platform used for collisions and walking. */
public class Platform {
    private final Rectangle rect;

    // Signature required by Level1: (Group, int, double, int)
    public Platform(Group root, int x, double y, int width) {
        this(root, x, y, width, 20);
    }
    public Platform(Group root, int x, double y, int width, int height) {
        rect = new Rectangle(width, height);
        rect.setTranslateX(x);
        rect.setTranslateY(y);
        try {
            Image tex = new Image("file:src/game/assets/images/world/platform.png");
            rect.setFill(new ImagePattern(tex, 0, 0, tex.getWidth(), tex.getHeight(), false));
        } catch (Throwable t) {
            rect.setFill(Color.GRAY);
        }
        root.getChildren().add(rect);
    }

    public Rectangle getShape() { return rect; }

    // Overloads to accept double X positions used by some levels
    public Platform(Group root, double x, double y, int width) {
        this(root, (int)Math.round(x), y, width, 20);
    }
    public Platform(Group root, double x, double y, int width, int height) {
        this(root, (int)Math.round(x), y, width, height);
    }
}