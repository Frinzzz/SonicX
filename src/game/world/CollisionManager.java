package game.world;

import javafx.scene.Group;
import javafx.geometry.Bounds;
import java.util.ArrayList;
import java.util.List;

import game.entities.Player;
import game.entities.EnemyBase;
import game.entities.Projectile;
import game.entities.Shield;
import game.audio.SoundManager;
import game.world.elements.*;
import game.world.ShieldPickup;

/**
 * Manages collisions and simple interactions.
 * - Robust landing detection to avoid tunneling when falling fast.
 * - Same landing logic applied to MovingPlatform.
 * - Recompute player bounds after snapping to ground.
 * - Handles spawn case when player's bottom is just above platform top (epsilon).
 * - Moving platforms carry the player horizontally (use mp.getDeltaX()).
 * - Skips ground/spring collisions while attached to a LoopDeLoop.
 */
public class CollisionManager {

    private final Player player;
    private final Group root;
    private final List<Projectile> projectiles = new ArrayList<>();

    private final List<Platform> platforms = new ArrayList<>();
    private final List<MovingPlatform> movingPlatforms = new ArrayList<>();
    private final List<Spring> springs = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<LoopDeLoop> loops = new ArrayList<>();
    private final List<Ring> rings = new ArrayList<>();
    private final List<ShieldPickup> shieldPickups = new ArrayList<>();
    private final List<EnemyBase> enemies = new ArrayList<>();

    public CollisionManager(Group root, Player player) {
        this.root = root;
        this.player = player;
    }

    public Player getPlayer() { return player; }

    public void addPlatform(Platform p) { platforms.add(p); }
    public void addMovingPlatform(MovingPlatform p) { movingPlatforms.add(p); }
    public void addSpring(Spring s) { springs.add(s); }
    public void addObstacle(Obstacle o) { obstacles.add(o); }
    public void addLoop(LoopDeLoop l) { loops.add(l); }
    public void addRing(Ring r) { rings.add(r); }
    public void addProjectile(Projectile p) { projectiles.add(p); }
    public void addEnemy(EnemyBase e) { enemies.add(e); }

    public void update(double dt) {
        // 0) update rings physics & cleanup
        for (int i = rings.size() - 1; i >= 0; i--) {
            Ring rr = rings.get(i);
            rr.update(dt);

            // simple ground/platform collision for dynamic rings (bounce)
            if (rr.isDynamic()) {
                javafx.geometry.Bounds rb = rr.getBounds();
                // collide with static platforms
                for (Platform p : platforms) {
                    javafx.geometry.Bounds pbp = p.getShape().getBoundsInParent();
                    if (rr.getVY() > 0 && rb.intersects(pbp)) {
                        double newY = pbp.getMinY() - rb.getHeight();
                        rr.setY(newY);
                        rr.setVY(-Math.abs(rr.getVY()) * 0.5);
                        rr.setVX(rr.getVX() * 0.95);
                        rb = rr.getBounds();
                    }
                }
                // collide with moving platforms
                for (MovingPlatform mp : movingPlatforms) {
                    javafx.geometry.Bounds pbp = mp.getShape().getBoundsInParent();
                    if (rr.getVY() > 0 && rb.intersects(pbp)) {
                        double newY = pbp.getMinY() - rb.getHeight();
                        rr.setY(newY);
                        rr.setVY(-Math.abs(rr.getVY()) * 0.5);
                        rr.setVX(rr.getVX() * 0.95);
                        rb = rr.getBounds();
                    }
                }
            }

            if (rr.isExpired()) {
                root.getChildren().remove(rr.getSprite());
                rings.remove(i);
            }
        }

        // 1) aggiorna piattaforme mobili
        for (MovingPlatform mp : movingPlatforms) {
            mp.update(dt);
        }

        // --- LoopDeLoop handling (prima di calcolare le collisioni col terreno) ---
        boolean inLoop = false;
        for (LoopDeLoop l : loops) {
            if (l.update(dt, player)) {
                inLoop = true;
            }
        }

        // Bounds del player (dopo eventuale update del loop)
        Bounds pb = player.getBounds();

        // Se NON siamo dentro un loop, eseguiamo terreno/pedane/springs
        if (!inLoop) {
            // 2) collisione player ↔ piattaforme
            boolean onGround = false;
            double vy = player.getVelocityY();

            final double PEN_TOLERANCE = 24.0;
            final double MIN_STEP_BACK = 1.0;

            // --- Piattaforme statiche
            for (Platform p : platforms) {
                Bounds b = p.getShape().getBoundsInParent();
                if ((pb.getMaxX() > b.getMinX()) && (pb.getMinX() < b.getMaxX())) {
                    double top = b.getMinY();
                    double bottom = pb.getMaxY();
                    double prevBottom = bottom - Math.max(MIN_STEP_BACK, vy * dt);

                    boolean crossedTopNow   = (vy >= 0 && prevBottom <= top && bottom >= top);
                    boolean withinTolerance = (vy >= 0 && bottom >= top && bottom <= top + PEN_TOLERANCE);
                    boolean grazingTop      = (bottom >= top - 1.0 && bottom < top && vy >= -1.0);

                    if (crossedTopNow || withinTolerance || grazingTop) {
                        double newCenterY = top - (pb.getHeight() / 2.0);
                        player.setCenter(player.getCenterX(), newCenterY);
                        player.setVelocityY(0);
                        onGround = true;
                        pb = player.getBounds(); // aggiorna per i check successivi
                    }
                }
            }

            // --- Head-bump con il soffitto delle piattaforme statiche ---
            for (Platform p : platforms) {
                Bounds b = p.getShape().getBoundsInParent();
                double inset = 2.0;
                double bMinX = b.getMinX() + inset;
                double bMaxX = b.getMaxX() - inset;

                if ((pb.getMaxX() > bMinX) && (pb.getMinX() < bMaxX)) {
                    double bottom = b.getMaxY();   // fondo della piattaforma (soffitto)
                    double top    = pb.getMinY();  // testa del player
                    double prevTop = top - Math.max(MIN_STEP_BACK, -vy * dt);

                    boolean crossedBottomNow = (vy < 0 && prevTop >= bottom && top <= bottom);
                    if (crossedBottomNow) {
                        double newCenterY = bottom + (pb.getHeight() / 2.0);
                        player.setCenter(player.getCenterX(), newCenterY + 0.5);
                        player.setVelocityY(0);
                        pb = player.getBounds();
                    }
                }
            }

            // --- Ground stick helper (chiude micro-gap quando cammini) ---
            if (!onGround) {
                double bestTop = Double.POSITIVE_INFINITY;
                Bounds bestBounds = null;
                for (Platform p : platforms) {
                    Bounds b = p.getShape().getBoundsInParent();
                    if ((pb.getMaxX() > b.getMinX()) && (pb.getMinX() < b.getMaxX())) {
                        double top = b.getMinY();
                        if (top >= pb.getMaxY() - 6 && top <= pb.getMaxY() + 6) {
                            if (top < bestTop) { bestTop = top; bestBounds = b; }
                        }
                    }
                }
                if (bestBounds != null && player.getVelocityY() >= 0) {
                    double newCenterY = bestTop - (pb.getHeight() / 2.0);
                    player.setCenter(player.getCenterX(), newCenterY);
                    player.setVelocityY(0);
                    onGround = true;
                    pb = player.getBounds();
                }
            }

            // --- Piattaforme mobili (con carry in X) ---
            for (MovingPlatform mp : movingPlatforms) {
                Bounds b = mp.getShape().getBoundsInParent();
                if ((pb.getMaxX() > b.getMinX()) && (pb.getMinX() < b.getMaxX())) {
                    double top = b.getMinY();
                    double bottom = pb.getMaxY();
                    double prevBottom = bottom - Math.max(MIN_STEP_BACK, vy * dt);

                    boolean crossedTopNow   = (vy >= 0 && prevBottom <= top && bottom >= top);
                    boolean withinTolerance = (vy >= 0 && bottom >= top && bottom <= top + PEN_TOLERANCE);
                    boolean grazingTop      = (bottom >= top - 1.0 && bottom < top && vy >= -1.0);

                    if (crossedTopNow || withinTolerance || grazingTop) {
                        double newCenterY = top - (pb.getHeight() / 2.0);

                        // *** CARRY ORIZZONTALE ***
                        double carriedX = player.getCenterX() + mp.getDeltaX();
                        player.setCenter(carriedX, newCenterY);

                        player.setVelocityY(0);
                        onGround = true;
                        pb = player.getBounds();
                    }
                }
            }

            player.setOnGround(onGround);

            // --- 3) Springs (atterraggio dall'alto, valida su tutta la larghezza) ---
            for (Spring s : springs) {
                Bounds sb = s.getBounds();

                // Ampiezza di sovrapposizione orizzontale (non solo un contatto puntiforme)
                double overlapX = Math.min(pb.getMaxX(), sb.getMaxX()) - Math.max(pb.getMinX(), sb.getMinX());
                if (overlapX <= 2.0) continue; // serve almeno 2px di overlap orizzontale

                double topS = sb.getMinY();
                double bottomP = pb.getMaxY();
                double prevBottomP = bottomP - Math.max(1.0, player.getVelocityY() * dt);

                // Tolleranza dinamica per frame skip (maggiore se si cade veloci)
                double eps = Math.max(6.0, Math.abs(player.getVelocityY()) * dt + 2.0);

                boolean comingDown = player.getVelocityY() >= 0;
                boolean crossedTop = (prevBottomP <= topS + eps) && (bottomP >= topS - eps);
                boolean verticalOk = (pb.getMinY() < sb.getMinY() + 8.0); // evita attivazioni quando si impatta di lato alto

                if (comingDown && crossedTop && verticalOk) {
                    // riposiziona esattamente sopra la molla per coerenza e per evitare re-penetrazione
                    double newCenterY = topS - (pb.getHeight() * 0.5);
                    player.setCenter(player.getCenterX(), newCenterY);

                    s.trigger();
                    try { SoundManager.playSfx("assets/audio/spring.wav"); } catch (Throwable ignored) {}

                    player.setVelocityY(-Math.abs(s.getPower()));
                    player.setOnGround(false);
                    pb = player.getBounds();
                }
            }

            // --- Blocco laterale contro le molle (evita di trapassarle dai lati) ---
            for (Spring s : springs) {
                Bounds sb = s.getBounds();
                // Se non c'è intersezione, salta
                if (!pb.intersects(sb)) continue;

                // Evita di interferire con l'atterraggio dall'alto, già gestito sopra
                boolean playerAboveTop = pb.getMaxY() <= sb.getMinY() + 2.0;
                if (playerAboveTop) continue;

                double vx = player.getVelocityX();
                double halfW = pb.getWidth() * 0.5;

                // Sovrapposizione verticale sufficiente (per non bloccare quando sfiora solo la testa o i piedi)
                boolean verticalOverlap = (pb.getMaxY() > sb.getMinY() + 6.0) && (pb.getMinY() < sb.getMaxY() - 6.0);

                if (verticalOverlap) {
                    // Entrata da sinistra -> blocca sul lato sinistro della molla
                    if (vx > 0 && pb.getMaxX() > sb.getMinX() && pb.getCenterX() < sb.getMinX()) {
                        double newCenterX = sb.getMinX() - halfW - 0.1;
                        player.setCenter(newCenterX, player.getCenterY());
                        player.setVelocityX(0);
                        pb = player.getBounds();
                    }
                    // Entrata da destra -> blocca sul lato destro della molla
                    else if (vx < 0 && pb.getMinX() < sb.getMaxX() && pb.getCenterX() > sb.getMaxX()) {
                        double newCenterX = sb.getMaxX() + halfW + 0.1;
                        player.setCenter(newCenterX, player.getCenterY());
                        player.setVelocityX(0);
                        pb = player.getBounds();
                    }
                }
            }

            // 4) Obstacles
            for (Obstacle o : obstacles) {
                if (pb.intersects(o.getBounds())) {
                    player.applyKnockback((o.getBounds().getMinX()+o.getBounds().getMaxX())*0.5);
                    player.takeDamage();
                }
            }
            
            // 6) Ring magnet (Electric shield)
            if (player != null && player.getShieldType() == Shield.Type.ELECTRIC) {
                double px = player.getCenterX();
                double py = player.getCenterY();
                double radius = 140.0;
                double pull = 520.0;
                for (Ring r : rings) {
                    if (!r.canBeCollected()) continue;
                    javafx.geometry.Bounds rb = r.getBounds();
                    double rx = (rb.getMinX()+rb.getMaxX())*0.5;
                    double ry = (rb.getMinY()+rb.getMaxY())*0.5;
                    double dx = px - rx;
                    double dy = py - ry;
                    double d2 = dx*dx + dy*dy;
                    if (d2 < radius*radius) {
                        double d = Math.max(Math.sqrt(Math.max(d2, 1e-3)), 1e-3);
                        double ax = (dx / d) * pull * dt;
                        double ay = (dy / d) * pull * dt;
                        r.setVX(r.getVX() * 0.9 + ax);
                        r.setVY(r.getVY() * 0.9 + ay);
                    }
                }
            }

            // 6.5) Shield pickups
            for (int i = shieldPickups.size()-1; i>=0; --i) {
                ShieldPickup sp = shieldPickups.get(i);
                if (pb.intersects(sp.getBounds())) {
                    player.equipShield(sp.getType());
                    sp.collect();
                    shieldPickups.remove(i);
                }
            }

            // 7) Rings (raccolta)
            for (Ring r : rings) {
                if (r.canBeCollected() && pb.intersects(r.getBounds())) {
                    r.collect();
                    player.addRings(1);
                    try { SoundManager.playSfx("/assets/sfx/ring.wav"); } catch (Throwable ignored) {}
                }
            }
        } // fine if (!inLoop)
        // 5) Enemies (sempre aggiornati, anche durante il loop)
        for (EnemyBase e : enemies) {
            e.update(dt);
        }
        // Gestione collisione player-nemici dopo l'aggiornamento
        for (EnemyBase e : enemies) {
            if (e.isAlive() && pb.intersects(e.getBounds())) {
                e.onPlayerCollision(player);
            }
        }
        // sweep dead enemies
        for (int ei = enemies.size() - 1; ei >= 0; --ei) {
            EnemyBase ee = enemies.get(ei);
            if (!ee.isAlive()) {
                if (ee.getSprite() != null && ee.getSprite().getParent() instanceof javafx.scene.Group) {
                    ((javafx.scene.Group)ee.getSprite().getParent()).getChildren().remove(ee.getSprite());
                }
                enemies.remove(ei);
            }
        }

        // 6) Projectiles (sempre aggiornati)
        for (int i = projectiles.size() - 1; i >= 0; --i) {
            Projectile p = projectiles.get(i);
            p.update(dt);
            if (!p.isAlive()) {
                root.getChildren().remove(p.getSprite());
                projectiles.remove(i);
                continue;
            }
            if (pb.intersects(p.getBounds())) {
                player.applyKnockback((p.getBounds().getMinX()+p.getBounds().getMaxX())*0.5);
                player.takeDamage();
                p.kill();
                root.getChildren().remove(p.getSprite());
                projectiles.remove(i);
            }
        }

    }

    @SuppressWarnings("unused")
    private boolean isStomp(Player player, javafx.geometry.Bounds enemyBounds) {
        javafx.geometry.Bounds pb = player.getBounds();
        boolean above = pb.getMaxY() <= enemyBounds.getMinY() + 6;
        boolean falling;
        try { falling = player.getVelocityY() > 0; } catch (Throwable __) { falling = true; }
        return above && falling;
    }
    public void addShieldPickup(ShieldPickup sp) { shieldPickups.add(sp); }
}
