package game.world.elements;

import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.geometry.Bounds;

/**
 * Molla (Spring) con hitbox affidabile.
 * - usa ImageView con posizione basata su translateX/translateY (come gli altri elementi)
 * - getBounds() sempre consistente con l'immagine
 * - trigger() con piccola animazione di compressione
 */
public class Spring {
    private final ImageView sprite;
    private final double power;

    /**
     * @param root  root node
     * @param x     posizione X (top-left) della molla
     * @param y     posizione Y (top-left) della molla
     * @param power velocità verso l'alto che verrà applicata al player
     */
    public Spring(Group root, double x, double y, double power) {
        this.power = power;

        Image img = null;
        try {
            // Percorso classpath (preferito)
            img = image("/assets/images/world/spring.png");
        } catch (Throwable ignored) {
            try {
                // Fallback in dev
                img = new Image("file:src/game/assets/images/world/spring.png");
            } catch (Throwable __) {
                // last resort: 1x1 transparent pixel to avoid NPE
                img = new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=");
            }
        }

        sprite = new ImageView(img);
        // Allinea come gli altri elementi del mondo (Rectangle usa translateX/Y)
        sprite.setTranslateX(x);
        sprite.setTranslateY(y);
        sprite.setPreserveRatio(false); // usa dimensioni reali dell'immagine
        sprite.setSmooth(true);

        root.getChildren().add(sprite);
    }

    /** Limiti dell'hitbox della molla (coincide con l'immagine). */
    public Bounds getBounds() {
        return sprite.getBoundsInParent();
    }

    /** Forza del rimbalzo verso l'alto. */
    public double getPower() {
        return power;
    }

    /** Accesso allo sprite se serve per debug o layering. */
    public ImageView getSprite() {
        return sprite;
    }

    /** Effetto di compressione rapido quando attivata. */
    public void trigger() {
        try {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), sprite);
            st.setFromY(1.0);
            st.setToY(0.7);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        } catch (Throwable ignored) {
            // Headless / nessuna animazione: nessun problema
        }
    }
}
