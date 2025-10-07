package game.entities;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;
import game.world.CollisionManager;

/**
 * Nemico che cammina avanti e indietro e spara due proiettili parabolici
 * (sinistra e destra) a intervalli regolari.
 *
 * - Movimento orizzontale identico al WalkerEnemy (oscillazione leggera).
 * - I proiettili sono registrati nel CollisionManager così infliggono danno
 *   e knockback al Player.
 * - Alla morte, i proiettili già sparati vengono "killati" per evitare
 *   che restino fermi sullo schermo.
 */
public class ShooterEnemy extends EnemyBase {
    private final Group root;
    private final CollisionManager cm;

    // movimento tipo WalkerEnemy
    private double speed = 1.2;

    // shooting
    private double shootCooldown = 1.6; // secondi tra raffiche
    private double tShoot = 0.0;
    private double bulletSpeedX = 140.0;
    private double bulletSpeedY = -180.0; // verso l'alto, poi la gravità del Projectile li curva

    // traccia dei miei proiettili per poterli killare alla morte
    private final List<Projectile> owned = new ArrayList<>();

    public ShooterEnemy(Group root, double x, double y) {
        this(root, x, y, null);
    }

    public ShooterEnemy(Group root, double x, double y, CollisionManager cm) {
        this.root = root;
        this.cm = cm;
        Image img = new Image("file:src/game/assets/images/entities/enemies/shooter.gif", 40, 40, true, true, true);
        sprite = new ImageView(img);
        sprite.setTranslateX(x);
        sprite.setTranslateY(y);
        root.getChildren().add(sprite);
    }

    @Override
    public void update(double dt) {
        if (!alive) return;

        // Walker-like oscillation
        sprite.setTranslateX(sprite.getTranslateX() + Math.sin(System.nanoTime() * 1e-9) * speed);

        // shooting timer
        tShoot += dt;
        if (tShoot >= shootCooldown) {
            tShoot = 0;
            shootPair();
        }
    }

    @Override
    public void onPlayerCollision(Player player) {
        // usa la logica base (stomp, danno, ecc.)
        super.onPlayerCollision(player);
        // se il nemico è morto via stomp, puliamo i proiettili ancora a schermo
        if (!alive) cleanupProjectiles();
    }

    // NOTA: EnemyBase non dichiara kill(), quindi niente @Override qui
    public void kill() {
        if (!alive) return;
        alive = false;
        cleanupProjectiles();
        if (sprite != null && sprite.getParent() instanceof Group) {
            ((Group) sprite.getParent()).getChildren().remove(sprite);
        }
    }

    private void shootPair() {
        if (!alive) return;
        double x = sprite.getTranslateX() + 20; // centro sprite
        double y = sprite.getTranslateY() + 8;

        // sinistra e destra
        Projectile left  = new Projectile(root, x, y, -bulletSpeedX, bulletSpeedY);
        Projectile right = new Projectile(root, x, y,  bulletSpeedX, bulletSpeedY);

        owned.add(left);
        owned.add(right);

        if (cm != null) {
            cm.addProjectile(left);
            cm.addProjectile(right);
        }

        try { game.audio.SoundManager.playSfx("/assets/sfx/shoot.wav"); } catch (Throwable ignored) {}
    }

    private void cleanupProjectiles() {
        for (int i = owned.size() - 1; i >= 0; --i) {
            Projectile p = owned.get(i);
            if (p != null && p.isAlive()) {
                p.kill();
                if (p.getSprite().getParent() instanceof Group) {
                    ((Group) p.getSprite().getParent()).getChildren().remove(p.getSprite());
                }
            }
            owned.remove(i);
        }
    }
}
