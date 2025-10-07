package game.world.elements;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/** Platform that oscillates along an axis. */
public class MovingPlatform {
    public enum Axis { HORIZONTAL, VERTICAL }

    private final Rectangle rect;
    private double deltaX = 0.0, deltaY = 0.0;
    private final Axis axis;
    private final double origin;
    private final double amplitude;
    private final double speed;
    private double t = 0.0;

    // Expected signature: (Group, x, y, width, Axis, amplitude, speed)
    public MovingPlatform(Group root, int x, double y, int width, Axis axis, double amplitude, double speed) {
        this.axis = axis;
        this.amplitude = amplitude;
        this.speed = speed;

        this.rect = new Rectangle(width, 20);

        if (axis == Axis.HORIZONTAL) {
            this.origin = x;
            rect.setTranslateX(x);
            rect.setTranslateY(y);
        } else {
            this.origin = y;
            rect.setTranslateX(x);
            rect.setTranslateY(y);
        }

        try {
            Image tex = new Image("file:src/game/assets/images/world/moving_platform.png");
            rect.setFill(new ImagePattern(tex, 0, 0, tex.getWidth(), tex.getHeight(), false));
        } catch (Throwable t) {
            rect.setFill(Color.DARKGRAY);
        }

        root.getChildren().add(rect);
    }

    /** Update platform position with delta time in seconds. */
    public void update(double dt) {
        // advance time and compute oscillation offset
        t += dt * speed;
        double offset = Math.sin(t) * amplitude;

        // start from current position
        double newX = rect.getTranslateX();
        double newY = rect.getTranslateY();

        // move along the chosen axis
        if (axis == Axis.HORIZONTAL) {
            newX = origin + offset;
        } else {
            newY = origin + offset;
        }

        // compute deltas relative to current transform
        deltaX = newX - rect.getTranslateX();
        deltaY = newY - rect.getTranslateY();

        // apply transform
        rect.setTranslateX(newX);
        rect.setTranslateY(newY);
    }

    public Rectangle getShape() { return rect; }
    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
}
