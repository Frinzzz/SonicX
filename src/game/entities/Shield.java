package game.entities;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Scudo visuale: cerchio semitrasparente che segue il player.
 * Niente asset incollati sopra al personaggio: solo grafica vettoriale.
 */
public class Shield {
    public enum Type { NORMAL, FIRE, ELECTRIC, BUBBLE }

    private final Group node = new Group();
    private final Type type;
    private double w = 56, h = 56; // area occupata (per centrare)

    public Shield(Group root, Type type) {
        this.type = (type == null ? Type.NORMAL : type);

        // Colori diversi per tipo (tutti con fill trasparente e bordo più visibile)
        Color stroke;
        Color fill;
        switch (this.type) {
            case FIRE -> {
                stroke = Color.web("#FF7043", 0.95);
                fill   = Color.web("#FF7043", 0.18);
            }
            case ELECTRIC -> {
                stroke = Color.web("#64B5F6", 0.98);
                fill   = Color.web("#64B5F6", 0.22);
            }
            case BUBBLE -> {
                stroke = Color.web("#4DB6AC", 0.95);
                fill   = Color.web("#4DB6AC", 0.20);
            }
            default -> {
                // azzurro trasparente (richiesta)
                stroke = Color.web("#4FC3F7", 0.98);
                fill   = Color.web("#4FC3F7", 0.22);
            }
        }

        double r = Math.max(w, h) * 0.45;
        Circle outer = new Circle(r);
        outer.setStroke(stroke);
        outer.setStrokeWidth(3.0);
        outer.setFill(fill);

        Circle inner = new Circle(r * 0.82);
        inner.setStroke(stroke.deriveColor(0,1,1,0.55));
        inner.setStrokeWidth(1.8);
        inner.setFill(Color.TRANSPARENT);

        DropShadow glow = new DropShadow();
        glow.setColor(stroke);
        glow.setRadius(12);
        outer.setEffect(glow);

        node.getChildren().addAll(outer, inner);

        if (root != null) root.getChildren().add(node);
        node.toFront();
        node.setVisible(true);
    }

    /** Posiziona lo scudo centrato sul player. */
    public void follow(double centerX, double centerY) {
        node.setTranslateX(centerX);
        node.setTranslateY(centerY);
    }

    /** Accesso al nodo JavaFX (per toFront/rimozione). */
    public Node getSprite() { return node; }

    public Type getType() { return type; }

    public void destroy() {
        if (node.getParent() instanceof Group g) {
            g.getChildren().remove(node);
        }
    }
}
