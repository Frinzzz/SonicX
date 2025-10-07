package game.levels;


import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import game.entities.Player;
import game.world.CollisionManager;

public interface Level {
    void build(Group parallax, Group root, Player player, CollisionManager cm);
    double getGroundY();
    double getFinishX();
}
