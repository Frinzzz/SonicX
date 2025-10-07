package game.world.elements;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;

public class FinishGate {
    private final ImageView sprite;

    public FinishGate(Group root, double x, double groundY) {
        Image img = new Image("file:src/game/assets/images/ui/level_finish.png", 64, 96, true, true, true);
        sprite = new ImageView(img);
        sprite.setTranslateX(x);
        sprite.setTranslateY(groundY - 96);
        root.getChildren().add(sprite);
    }

    public Bounds getBounds() { return sprite.getBoundsInParent(); }
    public ImageView getSprite() { return sprite; }
}
