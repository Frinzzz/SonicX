package game.entities;

import javafx.animation.FadeTransition;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import game.audio.SoundManager;
import game.entities.Shield;

/**
 * Player con movimento fluido in stile Sonic:
 * - accelerazione/decelerazione + attrito a terra
 * - coyote time + jump buffer (frame-based)
 * - animazioni: idle, run, jump, crouch, spindash (carica)
 * - spindash: tieni premuto S (crouch) e premi e tieni SPAZIO per caricare; al rilascio di SPAZIO parte lo spindash
 */
public class Player {

    // ===== Rendering =====
    private final ImageView sprite;
    private Image idleGif;
    private Image walkGif, runGif, skidGif, jumpUpGif, crouchGif, spinChargeGif;

    // ===== Damage / Rings / Context =====
    private game.world.CollisionManager damageCM;
    private Group damageWorld;
    private boolean deathQueued = false;
    private int rings = 0;
    private static final int MAX_RING_SCATTER = 20;
    private static final double RING_LOSS_IFRAMES = 1.5;
    private double invulnTimer = 0.0;

    
    // ===== Shield =====
    private Shield shield = null;
    private boolean airAbilityUsed = false;
    private boolean bubbleBouncing = false;
    public Shield getShield() { return shield; }
    public Shield.Type getShieldType() { return shield != null ? shield.getType() : null; }
    public boolean hasShield() { return shield != null; }
    public void equipShield(Shield.Type type) {
        Group root = (this.damageWorld != null ? this.damageWorld : (this.sprite != null ? (Group)this.sprite.getParent() : null));
        if (shield != null) { shield.destroy(); shield = null; }
        if (root != null) {
            shield = new Shield(root, type);
            airAbilityUsed = false;
            bubbleBouncing = false;
        }
    }
    public void removeShield() {
        if (shield != null) { shield.destroy(); shield = null; }
    }

    // ===== Knockback / Stun =====
    private double knockbackTimer = 0.0;
    private static final double KNOCKBACK_TIME = 0.30;   // seconds of reduced control
    private static final double KNOCKBACK_VX   = 260.0;  // horizontal impulse
    private static final double KNOCKBACK_VY   = 260.0;  // vertical impulse

    public boolean isStunned() { return knockbackTimer > 0.0; }

    public void applyKnockback(double fromX) {
        int dir = (getCenterX() < fromX) ? -1 : 1; // push away from source
        vx = dir * KNOCKBACK_VX;
        vy = -KNOCKBACK_VY;
        onGround = false;
        rolling = false;
        chargingSpin = false;
        knockbackTimer = KNOCKBACK_TIME;
        // non impostiamo invulnerabilità qui: la gestisce takeDamage()
        try {
            SoundManager.playSfx("/assets/sfx/hurt.wav");
        } catch (Throwable ignored) {}
    }

    private boolean damageQueued = false; // compat

    // ===== Input =====
    private boolean left, right, up, down;
    private boolean spaceDown = false;
    private boolean jumpPressedEdge = false;

    // ===== Stato fisico =====
    private double vx = 0.0, vy = 0.0;
    private double prevVy = 0.0;
    private double prevCenterY = 0.0;
    private boolean onGround = true;
    private int facing = 1;

    // ===== Roll / Spindash =====
    private boolean rolling = false;
    private boolean chargingSpin = false;
    private double spinCharge = 0.0;
    private static final double ROLL_MIN_SPEED   = 320.0;
    private static final double ROLL_FRICTION    = 160.0;
    private static final double SPIN_MAX         = 1200.0;
    private static final double SPIN_CHARGE_RATE = 1400.0;

    // ===== Parametri movimento =====
    private static final double TOP_SPEED_GROUND = 320.0;
    private static final double TOP_SPEED_AIR    = 360.0;
    private static final double ACC_GROUND       = 650.0;
    private static final double DECEL_GROUND     = 900.0;
    private static final double ACC_AIR          = 600.0;
    private static final double DECEL_AIR        = 800.0;
    private static final double GRAVITY          = 2200.0;
    private static final double MAX_FALL_SPEED   = 900.0;
    private static final double JUMP_SPEED       = 620.0;

    // Soglie animazioni a terra
    private static final double WALK_MAX_SPEED = 140.0;
    private static final double RUN_MIN_SPEED  = 180.0;
    private static final double SKID_SPEED_MIN = 110.0;
    private static final double SKID_FACTOR    = 0.30;
    private static final int    SKID_MIN_FRAMES = 16;

    // QoL jump (frame-based @60 FPS)
    private static final double BRAKE_DECEL      = 1100.0;
    private static final double MIN_SPEED_EPS    = 5.0;
    private static final int    COYOTE_FRAMES    = 6;
    private static final int    JUMP_BUFFER_FRAMES = 7;
    private int coyoteCounter = 0;
    private int skidTimer = 0;
    private int jumpBuffer = -1;

    // dt fisso ~1/60 per tick()
    private static final double DT = 1.0 / 60.0;

    // Dimensione uniforme di rendering
    private static final double FRAME_W = 32;
    private static final double FRAME_H = 32;

    public Player(Group root, double startX, double startY) {
        // Caricamento asset (best-effort)
        walkGif       = firstImage(
            "file:src/game/assets/images/entities/player/sonicwalk.gif"
        );
        idleGif       = firstImage(
            "file:src/game/assets/images/entities/player/standsonic.png"
        );
        runGif        = firstImage(
            "file:src/game/assets/images/entities/player/Sonic-run.gif"
        );
        skidGif       = firstImage(
            "file:src/game/assets/images/entities/player/Sonic_screech2.gif"
        );
        jumpUpGif     = firstImage(
            "file:src/game/assets/images/entities/player/jumpordashsonic.gif"
        );
        crouchGif     = firstImage(
            "file:src/game/assets/images/entities/player/Sonic_crouch2.gif"
        );
        spinChargeGif = firstImage(
            "file:src/game/assets/images/entities/player/chargedashsonic.gif"
        );

        Image initial = (idleGif != null ? idleGif
                        : (runGif != null ? runGif
                        : new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9lZ7p1EAAAAASUVORK5CYII=")));

        sprite = new ImageView(initial);
        sprite.setFitWidth(FRAME_W);
        sprite.setFitHeight(FRAME_H);
        sprite.setPreserveRatio(false);
        sprite.setSmooth(true);
        sprite.setTranslateX(startX);
        sprite.setTranslateY(startY);
        root.getChildren().add(sprite);
        sprite.setVisible(true);
        sprite.toFront();
    }

    /** Chiamato da GameApp subito dopo aver creato player e cm. */
    public void setDamageContext(game.world.CollisionManager cm, Group world) {
        this.damageCM = cm;
        this.damageWorld = world;
    }

    // ===== Input =====
    public void onKeyPressed(KeyCode code) {
        if (code == KeyCode.LEFT  || code == KeyCode.A) left  = true;
        if (code == KeyCode.RIGHT || code == KeyCode.D) right = true;
        if (code == KeyCode.DOWN  || code == KeyCode.S) down  = true;

        if (code == KeyCode.SPACE) {
        if (!onGround && shield != null && !airAbilityUsed) {
            Shield.Type st = shield.getType();
            if (st == Shield.Type.ELECTRIC) {
                // doppio salto/dash orizzontale
                double dash = 280.0;
                vx = (Math.abs(vx) > 0.1 ? Math.copySign(Math.max(Math.abs(vx), dash), vx) : (facing >= 0 ? dash : -dash));
                vy = Math.min(vy, -220.0);
                airAbilityUsed = true;
            } else if (st == Shield.Type.FIRE) {
                double dash = 360.0;
                vx = (Math.abs(vx) > 0.1 ? Math.copySign(Math.max(Math.abs(vx), dash), vx) : (facing >= 0 ? dash : -dash));
                vy = Math.min(vy, -120.0);
                airAbilityUsed = true;
            } else if (st == Shield.Type.BUBBLE) {
                // slam verso il basso che rimbalza a terra
                vy = Math.max(vy, 320.0);
                bubbleBouncing = true;
                airAbilityUsed = true;
            }
        }

            spaceDown = true;
            if (down && onGround) {
                chargingSpin = true;
                spinCharge = Math.max(spinCharge, 0.0);
            } else {
                if (!up) { jumpPressedEdge = true; jumpBuffer = JUMP_BUFFER_FRAMES; }
                up = true;
            }
        } else if (code == KeyCode.UP || code == KeyCode.W) {
            if (!up) { jumpPressedEdge = true; jumpBuffer = JUMP_BUFFER_FRAMES; }
            up = true;
        }
    }

    public void onKeyReleased(KeyCode code) {
        if (code == KeyCode.LEFT  || code == KeyCode.A) left  = false;
        if (code == KeyCode.RIGHT || code == KeyCode.D) right = false;
        if (code == KeyCode.DOWN  || code == KeyCode.S) down  = false;

        if (code == KeyCode.SPACE) {
            spaceDown = false;
            if (chargingSpin && down && onGround) {
                double dash = clamp(spinCharge * 1.1, ROLL_MIN_SPEED, SPIN_MAX);
                vx = (Math.abs(vx) > 0.1 ? Math.copySign(dash, vx) : (facing >= 0 ? dash : -dash));
                rolling = true;
                chargingSpin = false;
                spinCharge = 0.0;
                try { SoundManager.playSfx("/assets/sfx/spindash.wav"); } catch (Throwable ignored) {}
            } else {
                up = false;
            }
        }

        if (code == KeyCode.UP || code == KeyCode.W) up = false;
    }

    // ===== Game loop (tick a ~60 FPS) =====
    public void tick() {
        // salva stato precedente per lo stomp
        prevVy = vy;
        prevCenterY = getCenterY();

        // invulnerabilità
        if (knockbackTimer > 0) knockbackTimer -= 1.0/60.0;
        if (invulnTimer > 0) invulnTimer -= 1.0/60.0;

        // Timers coyote/buffer
        if (onGround) coyoteCounter = COYOTE_FRAMES;
        else if (coyoteCounter > 0) coyoteCounter--;
        if (jumpBuffer >= 0) jumpBuffer--;

        // Shield air ability reset on landing
        if (onGround) { airAbilityUsed = false; bubbleBouncing = false; }

        boolean stunned = (knockbackTimer > 0.0);

        // Orizzontale
        int inputDir = stunned ? 0 : ((right ? 1 : 0) - (left ? 1 : 0));
        double maxSpeed = onGround ? TOP_SPEED_GROUND : TOP_SPEED_AIR;
        double acc      = onGround ? ACC_GROUND      : ACC_AIR;
        double dec      = onGround ? DECEL_GROUND    : DECEL_AIR;

        if (inputDir != 0) {
            double desired = inputDir * maxSpeed;
            if (onGround && sign(vx) != 0.0 && sign(vx) != sign(desired)) {
                vx = moveTowards(vx, desired, BRAKE_DECEL * DT);
            } else {
                vx = moveTowards(vx, desired, acc * DT);
            }
        } else {
            if (!rolling) {
                vx = moveTowards(vx, 0.0, dec * DT);
                if (Math.abs(vx) < MIN_SPEED_EPS) vx = 0.0;
            }
        }

        if (!stunned) {
            if (inputDir != 0) {
                double desired = inputDir * maxSpeed;
                if (onGround && sign(vx) != 0.0 && sign(vx) != sign(desired)) {
                    vx = moveTowards(vx, desired, BRAKE_DECEL * DT);
                } else {
                    vx = moveTowards(vx, desired, acc * DT);
                }
            } else {
                vx = moveTowards(vx, 0.0, dec * DT);
                if (Math.abs(vx) < MIN_SPEED_EPS) vx = 0.0;
            }
        } else {
            // During knockback: keep current velocity; apply slight ground drag
            if (onGround) {
                double drag = DECEL_GROUND * 0.30 * DT;
                if (Math.abs(vx) <= drag) vx = 0.0; else vx -= Math.copySign(drag, vx);
            }
        }

        // clamp
        double speedCap = rolling ? SPIN_MAX : maxSpeed;
        if (vx >  speedCap) vx =  speedCap;
        if (vx < -speedCap) vx = -speedCap;

        // Skid
        boolean reversingNow = onGround && ((left && vx > 0) || (right && vx < 0));
        double speedAbs = Math.abs(vx);
        if (reversingNow && speedAbs > Math.max(SKID_SPEED_MIN, TOP_SPEED_GROUND * SKID_FACTOR)) {
            skidTimer = SKID_MIN_FRAMES;
        } else if (skidTimer > 0) {
            skidTimer--;
        }

        // Spindash charge
        if (!stunned) {
            if (chargingSpin) {
                if (down && onGround && spaceDown) {
                    spinCharge += SPIN_CHARGE_RATE * DT;
                    if (spinCharge > SPIN_MAX) spinCharge = SPIN_MAX;
                    vx = moveTowards(vx, 0.0, BRAKE_DECEL * DT);
                } else {
                    chargingSpin = false;
                    spinCharge = 0.0;
                }
            }
        }

        // Attrito roll & stop roll
        if (onGround && rolling && inputDir == 0) {
            double dv = ROLL_FRICTION * DT;
            if (Math.abs(vx) <= dv) vx = 0.0; else vx -= Math.copySign(dv, vx);
        }
        if (rolling && (!onGround || Math.abs(vx) < 30.0)) {
            rolling = false;
        }

        // Salto (buffer + coyote)
        if (!stunned) {
            boolean canJump = onGround || coyoteCounter > 0;
            if (jumpBuffer >= 0 && canJump) {
                vy = -JUMP_SPEED;
                onGround = false;
                coyoteCounter = 0;
                jumpBuffer = -1;
                try { SoundManager.playSfx("/assets/sfx/jump.wav"); } catch (Throwable ignored) {}
            }
        }

        // Gravità
        if (!onGround) {
            vy += GRAVITY * DT;
            if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;
        }

        // Integrazione
        sprite.setTranslateX(sprite.getTranslateX() + vx * DT);
        sprite.setTranslateY(sprite.getTranslateY() + vy * DT);

        // Animazioni
        updateAnimation();

        // reset rising-edge
        jumpPressedEdge = false;
    
        // update shield visual
        if (shield != null) {
            shield.follow(getCenterX(), getCenterY());
            shield.getSprite().toFront();
        }
    }


    // ===== API attese dal resto del progetto =====
    public ImageView getSprite() { return sprite; }
    public Bounds getBounds() { return sprite.getBoundsInParent(); }
    public boolean isRolling() { return rolling; }
    public void setOnGround(boolean value) { this.onGround = value; }
    public double getVelocityY() { return vy; }
    public double getLastVelocityY() { return prevVy; }
    public double getLastCenterY() { return prevCenterY; }
    public double getVelocityX() { return vx; }
    public void setVelocityY(double value) { this.vy = value; }
    public void setVelocityX(double value) { this.vx = value; }

    public double getCenterX() {
        Bounds b = sprite.getBoundsInParent();
        return (b.getMinX() + b.getMaxX()) * 0.5;
    }

    public double getCenterY() {
        Bounds b = sprite.getBoundsInParent();
        return (b.getMinY() + b.getMaxY()) * 0.5;
    }

    public void setCenter(double cx, double cy) {
        Bounds local = sprite.getBoundsInLocal();
        double w = local.getWidth();
        double h = local.getHeight();
        sprite.setTranslateX(cx - w * 0.5);
        sprite.setTranslateY(cy - h * 0.5);
    }

    // ===== Danno / Invulnerabilità / Anelli =====
    private void flash(double seconds) {
        FadeTransition ft = new FadeTransition(Duration.seconds(seconds), sprite);
        ft.setFromValue(1.0);
        ft.setToValue(0.2);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.play();
    }

    /** Gestisce la perdita anelli o la morte. */
    public void takeDamage() {
        // Se siamo ancora invulnerabili, ignora ulteriori colpi
        if (invulnTimer > 0) return;

        // Se abbiamo uno scudo attivo, viene consumato e non perdiamo anelli
        if (shield != null) {
            removeShield();
            setInvulnerable(1.0);
            return;
        }

        // Se abbiamo ring, li perdiamo tutti con scatter, altrimenti si muore
        if (rings > 0) {
            int toDrop = Math.min(rings, MAX_RING_SCATTER);
            double px = getCenterX();
            double py = getCenterY();

            // Se abbiamo il contesto per spawnare i ring dinamici, creali
            if (damageCM != null && damageWorld != null) {
                for (int i = 0; i < toDrop; i++) {
                    double angle = (Math.PI * 2.0) * (i / (double) toDrop);
                    double rvx = Math.cos(angle) * 160.0 + (Math.random() - 0.5) * 60.0;
                    double rvy = -Math.abs(Math.sin(angle) * 220.0) - 80.0 * Math.random();
                    double ox  = Math.cos(angle) * 18.0;
                    double oy  = Math.sin(angle) * 14.0;
                    damageCM.addRing(new game.world.Ring(damageWorld, px + ox, py - 10 + oy, rvx, rvy, 3.0));
                }
            }
            rings = 0;
            setInvulnerable(RING_LOSS_IFRAMES);
            try { SoundManager.playSfx("/assets/sfx/ring_loss.wav"); } catch (Throwable ignored) {}
        } else {
            deathQueued = true;
        }
    }

    // Rings / Invulnerabilità
    public int getRings() { return rings; }
    public void setRings(int n) { rings = Math.max(0, n); }
    public void addRings(int n) { rings = Math.max(0, rings + n); }
    public boolean isInvulnerable() { return invulnTimer > 0; }
    public void setInvulnerable(double seconds) { invulnTimer = Math.max(invulnTimer, seconds); flash(seconds); }

    // Compatibilità
    public boolean consumeDamageQueued() { boolean b = damageQueued; damageQueued = false; return b; }
    public boolean consumeDeathQueued() { boolean b = deathQueued; deathQueued = false; return b; }

    // ===== Helpers =====
    private static Image firstImage(String... uris) {
        for (String u : uris) {
            try {
                Image img = new Image(u);
                if (!img.isError() && img.getWidth() > 0) return img;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double sign(double x) { return x > 0 ? 1.0 : (x < 0 ? -1.0 : 0.0); }

    private static double moveTowards(double current, double target, double maxDelta) {
        double delta = target - current;
        if (Math.abs(delta) <= maxDelta) return target;
        return current + Math.copySign(maxDelta, delta);
    }

    private void setAnimation(Image img) {
        if (img == null) return;
        if (sprite.getImage() != img) sprite.setImage(img);
    }

    private void updateAnimation() {
        int inputDir = (right ? 1 : 0) - (left ? 1 : 0);
        if (inputDir != 0)      facing = inputDir;
        else if (Math.abs(vx) > 1.0) facing = (vx > 0 ? 1 : -1);
        sprite.setScaleX(facing);

        Image target;
        double speed = Math.abs(vx);

        if (!onGround) {
            target = (jumpUpGif != null ? jumpUpGif : (idleGif != null ? idleGif : sprite.getImage()));
        } else if (chargingSpin) {
            target = (spinChargeGif != null ? spinChargeGif : (idleGif != null ? idleGif : sprite.getImage()));
        } else {
            boolean reversing = (left && vx > 0) || (right && vx < 0);
            boolean doSkid = (skidTimer > 0) || (reversing && speed > Math.max(SKID_SPEED_MIN, TOP_SPEED_GROUND * SKID_FACTOR));
            if (doSkid && skidGif != null) {
                target = skidGif;
            } else if (speed > RUN_MIN_SPEED) {
                target = (runGif != null ? runGif : (walkGif != null ? walkGif : (idleGif != null ? idleGif : sprite.getImage())));
            } else if (speed > 20) {
                target = (walkGif != null ? walkGif : (runGif != null ? runGif : (idleGif != null ? idleGif : sprite.getImage())));
            } else {
                target = (down && crouchGif != null) ? crouchGif : (idleGif != null ? idleGif : sprite.getImage());
            }
        }

        setAnimation(target);
    }
}
