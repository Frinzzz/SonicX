package game.world;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Background {
    private final ImageView img;
    public Background(Group root, String path, double w, double h) {
        this(root, path, w, h, 0);
    }
    public Background(Group root, String path, double w, double h, double x) {
        Image image = new Image(path, w, h, false, true, true);
        img = new ImageView(image);
        img.setTranslateX(x);
        root.getChildren().add(img);
    }
}
