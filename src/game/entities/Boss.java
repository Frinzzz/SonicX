package game.entities;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import game.world.CollisionManager;
import game.entities.Projectile;
import game.entities.Player;

import java.lang.reflect.Constructor;

/**
 * Boss con stati: PATROL -> SHOOT -> DASH.
 * - Movimento ondulatorio (enableWaveMovement)
 * - Spari a raffiche (enableShooting)
 * - Sprite di fallback (nessuna dipendenza da asset esterni)
 * - Creazione proiettile compatibile con più firme costruttore via reflection
 */
public class Boss extends EnemyBase {
    private double invulnTimer = 0.0; // seconds of invulnerability after a hit

    private boolean defeated = false;

    // --- World / rendering ---
    private final Group root;
    private final CollisionManager cm;

    // --- State machine ---
    private enum State { PATROL, SHOOT, DASH }
    private State state = State.PATROL;
    private double stateTime = 0.0;

    // --- Stats / parametri base ---
    private int hp = game.Constants.BOSS_LIVES;
    private double baseX;
    private double range = 180;          // fallback oscillazione orizzontale
    private double hoverPhase = 0;       // piccola oscillazione verticale
    private double hoverSpeed = 1.8;
    private double hoverAmp = 8;

    // --- Waypoints (fallback opzionale) ---
    private double[][] waypoints;
    private int wpIndex = 0;

    // --- Movimento ondulatorio ---
    private boolean useWave = false;
    private double waveCx, waveCy;
    private double waveAmpX = 0, waveAmpY = 0;
    private double waveOmegaX = 0, waveOmegaY = 0; // rad/s
    private double wavePhase = 0; // rad
    private double waveTime = 0;  // s

    // --- Sparo ---
    private boolean canShoot = false;
    private double shootCooldown = 1.4;  // tempo tra raffiche
    private double shootTimer = 0.0;
    private int    burstSize = 3;        // colpi per raffica
    private int    burstLeft = 0;
    private double burstGap = 0.12;      // intervallo fra colpi nella stessa raffica
    private double burstTimer = 0.0;
    private double projSpeed = 260;      // px/s
    private boolean shootRight = true;   // alterna dx/sx se non si mira al player

    // ------------------------------------------------------------------------

    public Boss(Group root, double startX, double startY, CollisionManager cm) {
        this(root, startX, startY, cm, null);
    }

    public Boss(Group root, double startX, double startY, CollisionManager cm, double[][] waypoints) {
        this.root = root;
        this.cm = cm;
        this.waypoints = waypoints;

        // Sprite di fallback 48x48 (bordo giallo, riempimento rosso scuro)
        WritableImage img = new WritableImage(48, 48);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < 48; y++) {
            for (int x = 0; x < 48; x++) {
                boolean border = (x < 2 || y < 2 || x >= 46 || y >= 46);
                pw.setColor(x, y, border ? Color.YELLOW : Color.DARKRED);
            }
        }
        this.sprite = new ImageView(img);
        this.sprite.setFitWidth(48);
        this.sprite.setFitHeight(48);
        this.sprite.setTranslateX(startX);
        this.sprite.setTranslateY(startY - 48);
        this.baseX = startX;

        root.getChildren().add(this.sprite);
    }

    // --- Accessors usati da GameApp/HUD ---
    public int getHp() { return hp; }
    public boolean isDefeated() { return hp <= 0 || !alive; }

    // --- Config sparo ---
    public void enableShooting() { this.canShoot = true; }
    public void enableShooting(double cooldown, double projectileSpeed, double gap, int burst) {
        this.canShoot = true;
        this.shootCooldown = Math.max(0.2, cooldown);
        this.projSpeed = Math.max(60, projectileSpeed);
        this.burstGap = Math.max(0.05, gap);
        this.burstSize = Math.max(1, burst);
    }

    // --- Update --------------------------------------------------------------
    @Override
    public void update(double dt) {
        if (invulnTimer > 0) invulnTimer = Math.max(0, invulnTimer - dt);
        if (!alive) return;

        stateTime += dt;

        switch (state) {
            case PATROL:
                patrol(dt);
                if (stateTime > 2.6) switchState(State.SHOOT);
                break;

            case SHOOT:
                patrol(dt);         // continua l’onda anche mentre spara
                handleShooting(dt);
                if (!isShootingBurst() && stateTime > shootCooldown * 0.4) {
                    switchState(State.DASH);
                }
                break;

            case DASH:
                double x = sprite.getTranslateX();
                double dash = Math.sin(stateTime * Math.PI * 2.0) * 2.2;
                sprite.setTranslateX(x + dash);
                hover(dt);
                if (stateTime > 1.2) switchState(State.PATROL);
                break;
        }
    }

    private void switchState(State next) { state = next; stateTime = 0.0; }

    // --- Movimento -----------------------------------------------------------
    private void patrol(double dt) {
        if (useWave) {
            waveTime += dt;
            double x = waveCx + waveAmpX * Math.sin(waveTime * waveOmegaX);
            double y = waveCy + waveAmpY * Math.sin(waveTime * waveOmegaY + wavePhase);
            sprite.setTranslateX(x);
            sprite.setTranslateY(y);
            return;
        }
        if (waypoints != null && waypoints.length > 0) {
            moveTowardWaypoint(dt);
            return;
        }
        double t = stateTime;
        double offset = Math.sin(t * 1.2) * range * 0.5;
        sprite.setTranslateX(baseX + offset);
        hover(dt);
    }

    private void hover(double dt) {
        hoverPhase += dt * hoverSpeed;
        double hover = Math.sin(hoverPhase) * hoverAmp;
        sprite.setTranslateY(sprite.getTranslateY() + hover * dt);
    }

    private void moveTowardWaypoint(double dt) {
        double tx = waypoints[wpIndex][0];
        double ty = waypoints[wpIndex][1];
        double cx = sprite.getTranslateX();
        double cy = sprite.getTranslateY();

        double dx = tx - cx;
        double dy = ty - cy;
        double dist = Math.hypot(dx, dy);
        if (dist < 2.0) {
            wpIndex = (wpIndex + 1) % waypoints.length;
            return;
        }
        double speed = 100; // px/s
        double vx = (dx / (dist + 1e-6)) * speed * dt;
        double vy = (dy / (dist + 1e-6)) * speed * dt;
        sprite.setTranslateX(cx + vx);
        sprite.setTranslateY(cy + vy);
    }

    // --- Shooting ------------------------------------------------------------
    private boolean isShootingBurst() { return burstLeft > 0; }

    private void handleShooting(double dt) {
        if (!canShoot) return;

        if (burstLeft > 0) {
            burstTimer -= dt;
            if (burstTimer <= 0) {
                fireProjectile();
                burstLeft--;
                burstTimer = (burstLeft > 0) ? burstGap : 0.0;
            }
            return;
        }

        shootTimer -= dt;
        if (shootTimer <= 0) {
            burstLeft = burstSize;
            burstTimer = 0.0;           // primo colpo immediato
            shootTimer = shootCooldown; // reset per la prossima raffica
        }
    }

    private void fireProjectile() {
        double cx = sprite.getBoundsInParent().getMinX()
                  + sprite.getBoundsInParent().getWidth() * 0.5;
        double cy = sprite.getBoundsInParent().getMinY()
                  + sprite.getBoundsInParent().getHeight() * 0.5;

        Player pl = cm.getPlayer();
        double tx = pl.getCenterX();
        double ty = pl.getCenterY();
        double dx = tx - cx;
        double dy = ty - cy;
        double len = Math.hypot(dx, dy);
        double vx = (len > 1e-3 ? (dx / len) : (shootRight ? 1 : -1)) * projSpeed;
        double vy = (len > 1e-3 ? (dy / len) : 0) * projSpeed;
        shootRight = !shootRight;

        Projectile p = newProjectileSmart(cx, cy, vx, vy);
        if (p != null) cm.addProjectile(p);
    }

    /**
     * Crea un Projectile provando automaticamente TUTTI i costruttori pubblici
     * e mappando gli argomenti in modo "intelligente".
     */
    private Projectile newProjectileSmart(double x, double y, double vx, double vy) {
        try {
            Constructor<?>[] ctors = Projectile.class.getConstructors();
            for (Constructor<?> c : ctors) {
                Class<?>[] types = c.getParameterTypes();
                Object[] args = new Object[types.length];
                int usedDoubles = 0;
                boolean compatible = true;

                for (int i = 0; i < types.length; i++) {
                    Class<?> t = types[i];

                    if (Group.class.isAssignableFrom(t)) {
                        args[i] = root;
                    } else if (CollisionManager.class.isAssignableFrom(t)) {
                        args[i] = cm;
                    } else if (t == double.class || t == Double.TYPE) {
                        double val = (usedDoubles == 0) ? x
                                   : (usedDoubles == 1) ? y
                                   : (usedDoubles == 2) ? vx
                                   : (usedDoubles == 3) ? vy
                                   : 0.0;
                        args[i] = val;
                        usedDoubles++;
                    } else if (t == int.class || t == Integer.TYPE) {
                        args[i] = 0;
                    } else if (t == boolean.class || t == Boolean.TYPE) {
                        args[i] = false;
                    } else if (t.getName().equals("javafx.scene.paint.Color")) {
                        args[i] = Color.WHITE;
                    } else {
                        args[i] = null;
                    }
                }

                try {
                    Object obj = c.newInstance(args);
                    if (obj instanceof Projectile) return (Projectile) obj;
                } catch (Throwable ignore) {
                    compatible = false;
                }

                if (!compatible) continue;
            }
        } catch (Throwable ignore) {
            // fallback -> null
        }
        return null;
    }
    // ------------------------------------------------------------------------

    /** Abilita il movimento ondulatorio stile Sonic Mania. */
    public void enableWaveMovement(double cx, double cy, double ampX, double ampY,
                                   double periodX, double periodY, double phase) {
        this.useWave   = true;
        this.waveCx    = cx;
        this.waveCy    = cy;
        this.waveAmpX  = Math.max(0, ampX);
        this.waveAmpY  = Math.max(0, ampY);
        this.waveOmegaX = (periodX > 0) ? (2 * Math.PI / periodX) : 0;
        this.waveOmegaY = (periodY > 0) ? (2 * Math.PI / periodY) : 0;
        this.wavePhase  = phase;
        this.waveTime   = 0;
    }

    // --- Collisione con il player -------------------------------------------
    @Override
    public void onPlayerCollision(Player player) {
        if (!alive || isDefeated()) return;

        // Stomp dall'alto oppure impatto in roll/spindash
        if (isStompFromAbove(player) || player.isRolling()) {
            onHit();

            if (player.isRolling()) {
                // rimbalzo orizzontale + piccolo bump verso l'alto
                try {
                    double bossCx = (getBounds().getMinX()+getBounds().getMaxX())*0.5;
                    double dir = (player.getCenterX() >= bossCx) ? +1.0 : -1.0; // spingi via dal boss
                    double vxAbs = Math.max(260.0, Math.abs(player.getVelocityX()));
                    player.setVelocityX(dir * vxAbs);
                    player.setVelocityY(-240.0);
                } catch (Throwable __) {}
            } else {
                // stomp classico: rimbalzo verticalmente
                try { player.setVelocityY(-420.0); } catch (Throwable __) {}
            }
        } else {
            // non stomp e non roll: il boss colpisce il player
            try { player.applyKnockback((getBounds().getMinX()+getBounds().getMaxX())*0.5); } catch (Throwable __) {}
            player.takeDamage();
        }
    }

    public void hit() { onHit(); }

    private boolean isStompFromAbove(Player player) {
        Bounds pb = player.getBounds();
        Bounds bb = sprite.getBoundsInParent();
        boolean above = pb.getMaxY() <= bb.getMinY() + 6;
        boolean falling = (player.getVelocityY() > 0);
        return above && falling;
    }

    // Gestisce un colpo al boss (stomp o proiettile)
    public void onHit() {
        // invulnerabilità breve per evitare hit multipli nello stesso frame
        if (invulnTimer > 0) return;
        invulnTimer = 0.6; // 600 ms di i-frame

        // decrementa HP
        hp = Math.max(0, hp - 1);

        try { game.audio.SoundManager.playSfx("/assets/sfx/hit.wav"); } catch (Throwable ignored) {}

        // feedback visivo semplice: leggero flash rosso se possibile
        try {
            javafx.scene.effect.ColorAdjust adj = new javafx.scene.effect.ColorAdjust();
            adj.setHue(-0.5);
            adj.setSaturation(0.6);
            adj.setBrightness(0.3);
            sprite.setEffect(adj);
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.12));
            pt.setOnFinished(e -> sprite.setEffect(null));
            pt.play();
        } catch (Throwable ignored) {}

        if (hp <= 0) {
            defeated = true;
            alive = false;
            // rimuovi dallo stage
            try {
                if (sprite != null && sprite.getParent() instanceof Group) {
                    ((Group) sprite.getParent()).getChildren().remove(sprite);
                }
            } catch (Throwable ignored) {}
            // eventuale suono di sconfitta
            try { game.audio.SoundManager.playSfx("/assets/sfx/boss_defeat.wav"); } catch (Throwable ignored) {}
        }
    }
}
