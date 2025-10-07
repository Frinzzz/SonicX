package game.world.elements;

import static game.util.ResourceUtil.*;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Bounds;
import game.entities.Player;

/**
 * LoopDeLoop "classico" (stile Sonic):
 * - Snap/attach quando il player entra dal lato con sufficiente velocità.
 * - Movimento vincolato lungo una circonferenza con accelerazione tangenziale dovuta alla gravità.
 * - Se la velocità è insufficiente vicino alla sommità, il player si stacca e cade.
 * - Uscita automatica nel quadrante in basso sul lato opposto all'entrata.
 *
 * Nota: l'aggiornamento viene richiamato da CollisionManager.update()
 *       tramite l.update(dt, player). Se nessun loop è attaccato, la chiamata
 *       ritorna semplicemente false.
 */
public class LoopDeLoop {
    private final ImageView sprite;
    private final ImageView follower;   // piccolo sprite che segue la circonferenza
    private final Circle collider;

    // Stato fisico/geom.
    private final double cx;
    private final double cy;
    private final double r;

    // Stato di attacco
    private boolean attached = false;
    // Cooldown per evitare ri-aggancio immediato
    private double reattachCooldown = 0.0;
    private static final double REATTACH_COOLDOWN = 0.50;

    // SEMPLICE ANIMAZIONE: un giro predefinito intorno al cerchio
    private boolean simpleAnimating = false;
    private double animTime = 0.0;
    private double animDuration = 1.20; // secondi per un giro
    private double omegaAnim = (Math.PI * 2.0) / animDuration; // rad/s
    private int dir = -1;               // -1 = senso orario (da sinistra verso destra), +1 = antiorario
    private double theta = Math.PI;     // angolo corrente (rad), 0 = lato destro, PI = lato sinistro
    private double speed = 0.0;         // velocità tangenziale in px/s

    // Config: consenti attacco dai lati? (richiesto: NO)
    private static final boolean ALLOW_SIDE_ENTRY = false;

    // Costanti (coerenti con Player)
    private static final double G = 2200.0;              // GRAVITY del Player
    private static final double ENTRY_MIN_SPEED = 260.0; // velocità minima per attaccarsi
    private static final double FRICTION = 0.0;          // nessun attrito dentro il loop (fluidità)
    private static final double MIN_TOP_FACTOR = 0.45;   // soglia più permissiva per completare il giro
    private static final double SNAP_TOL = 26.0;         // tolleranza per snap lato ingresso
    private static final double TOP_DETACH_SAFETY = 0.92;// quanto vicino alla sommità consideriamo "top"
    private static final double EXIT_Y_PORTION = 0.20;   // deve essere sotto il centro di almeno 20% r per staccare

    // Signature richiesta dal progetto: (Group, int, double, int)
    public LoopDeLoop(Group root, int x, double y, int radius) {
        Image img = new Image("file:src/game/assets/images/world/loop.png",
                radius * 2, radius * 2, true, true, true);
        sprite = new ImageView(img);
        sprite.setTranslateX(x - radius);
        sprite.setTranslateY(y - radius);
        root.getChildren().add(sprite);

        // Sprite segnapercorso che seguirà il loop (decorativo)
        Image ringImg = new Image("file:src/game/assets/images/world/ring.png",
                24, 24, true, true, true);
        follower = new ImageView(ringImg);
        follower.setVisible(false);
        follower.setTranslateX(x - 12);
        follower.setTranslateY(y - radius - 12);
        root.getChildren().add(follower);

        collider = new Circle(x, y, radius);
        collider.setStroke(Color.TRANSPARENT);
        collider.setFill(Color.TRANSPARENT);
        root.getChildren().add(collider);

        this.cx = x;
        this.cy = y;
        this.r  = radius;
    }

    public Bounds getBounds() { return collider.getBoundsInParent(); }
    public double getCenterX() { return cx; }
    public double getCenterY() { return cy; }
    public double getRadius()  { return r; }

    /** Ritorna true se in questo frame il player è attaccato al loop (agganciato). */
    public boolean update(double dt, Player p) {
        // gestisci cooldown di ri-aggancio
        if (reattachCooldown > 0) reattachCooldown -= dt;
        if (!attached) {
            if (reattachCooldown <= 0 && shouldAttach(p)) {
                attach(p);
                // dopo l'attach aggiorniamo direttamente una volta per evitare jitter
                doUpdate(dt, p);
                return true;
            }
            return false;
        } else {
            doUpdate(dt, p);
            return attached;
        }
    }

    /** Verifica se il player entra nelle finestre di attacco (sinistra, destra o basso). */
    private boolean shouldAttach(Player p) {
        // Centro player
        double px = p.getCenterX();
        double py = p.getCenterY();
        double vx = p.getVelocityX();
        double vy = p.getVelocityY();

        // Finestre di ingresso laterali (intorno al centro verticale del loop)
        boolean nearLeft  = (px >= cx - r - 60) && (px <= cx - r + SNAP_TOL + 12) && (Math.abs(py - cy) <= r * 0.52);
        boolean nearRight = (px <= cx + r + 60) && (px >= cx + r - SNAP_TOL - 12) && (Math.abs(py - cy) <= r * 0.52);

        // Finestra di ingresso dal basso (intorno al centro orizzontale del loop)
        boolean nearBottom = (py <= cy + r + 60) && (py >= cy + r - (SNAP_TOL + 20)) && (Math.abs(px - cx) <= r * 0.60);

        // *** FINESTRA STRETTA IN BASSO-CENTRALE (attivazione camminando) ***
        boolean nearBottomCenter =
                (Math.abs(px - cx) <= Math.max(18.0, r * 0.18)) &&
                (py >= cy + r - 26) && (py <= cy + r + 20);

        // Attivazione semplice quando cammina sul centro in basso del loop
        if (nearBottomCenter && Math.abs(vx) > 60.0) {
            dir = (vx >= 0) ? -1 : +1; // determina senso in base alla direzione di cammino
            theta = Math.PI * 0.5;    // parte dal fondo
            speed = Math.max(Math.abs(vx), ENTRY_MIN_SPEED);
            return true;
        }

        // Ingresso da sinistra verso destra (disabilitato)
        if (ALLOW_SIDE_ENTRY && nearLeft && vx > 0) {
            dir = -1;          // orario
            theta = Math.PI;   // sinistra
            speed = Math.max(Math.abs(vx), ENTRY_MIN_SPEED);
            return true;
        }

        // Ingresso da destra verso sinistra (disabilitato)
        if (ALLOW_SIDE_ENTRY && nearRight && vx < 0) {
            dir = +1;          // antiorario
            theta = 0.0;       // destra
            speed = Math.max(Math.abs(vx), ENTRY_MIN_SPEED);
            return true;
        }

        // Ingresso dal basso: deve salire (vy negativo), direzione in base al segno di vx
        if (nearBottom && (Math.abs(vy) < 80 || vy < -ENTRY_MIN_SPEED * 0.5)) {
            theta = Math.PI * 0.5; // punto più in basso
            dir = (vx >= 0) ? -1 : +1; // vx>0 => orario; vx<0 => antiorario
            // velocità tangenziale iniziale coerente almeno con la minima di ingresso
            speed = Math.max(Math.abs(vx), ENTRY_MIN_SPEED);
            return true;
        }

        return false;
    }

    private void attach(Player p) {
        // modalità animazione semplice
        simpleAnimating = true;
        animTime = 0.0;
        // durata proporzionale alla velocità orizzontale (opzionale)
        double v = Math.abs(p.getVelocityX());
        double minDur = 0.9, maxDur = 1.4;
        double t = 1.20;
        if (v > 50) {
            // mappa 50..360 px/s -> 1.3..0.9 s
            double a = Math.max(50.0, Math.min(360.0, v));
            t = maxDur - (a - 50.0) * (maxDur - minDur) / (360.0 - 50.0);
        }
        animDuration = t;
        omegaAnim = (Math.PI * 2.0) / animDuration;

        p.setOnGround(false);
        attached = true;

        // Snap pos
        double x = cx + r * Math.cos(theta);
        double y = cy + r * Math.sin(theta);
        p.setCenter(x, y);

        // garantisci una velocità minima per poter chiudere il giro
        double vMinAttach = Math.sqrt(G * r * MIN_TOP_FACTOR) + 30.0;
        if (speed < vMinAttach) speed = vMinAttach;

        // Velocità tangenziale iniziale coerente
        setTangentialVelocity(p);

        // attiva e posiziona il follower
        if (follower != null) {
            follower.setVisible(true);
            follower.setTranslateX(x - 12);
            follower.setTranslateY(y - 12);
            follower.setRotate(Math.toDegrees(theta));
        }
    }

    private void detach(Player p) {
        attached = false;
        reattachCooldown = REATTACH_COOLDOWN;
        try { p.getSprite().setRotate(0); } catch (Throwable ignored) {}

        // Manteniamo la velocità tangenziale corrente al momento dello stacco
        setTangentialVelocity(p);
        p.setOnGround(false);

        // nascondi follower in uscita
        if (follower != null) {
            follower.setVisible(false);
        }
    }

    private void setTangentialVelocity(Player p) {
        // Versore tangente per angolo theta con y verso il basso:
        // CCW (dir=+1): t = (-sinθ, cosθ)
        // CW  (dir=-1): t = ( sinθ,-cosθ)  => in generale: t = dir * (-sinθ, cosθ)
        double tX = dir * (-Math.sin(theta));
        double tY = dir * ( Math.cos(theta));
        p.setVelocityX(speed * tX);
        p.setVelocityY(speed * tY);
    }

    private void doUpdate(double dt, Player p) {
        if (!attached) return;
        p.setOnGround(false);

        if (simpleAnimating) {
            animTime += dt;

            // avanza angolo con velocità angolare costante
            theta += (dir * omegaAnim) * dt;

            // posizione sul cerchio
            double x = cx + r * Math.cos(theta);
            double y = cy + r * Math.sin(theta);
            p.setCenter(x, y);

            // allinea velocità alla tangente per continuità quando si stacca
            setTangentialVelocity(p);

            // ruota lo sprite per l'effetto visivo
            try { p.getSprite().setRotate(Math.toDegrees(theta)); } catch (Throwable ignored) {}

            if (animTime >= animDuration) {
                // termina un giro
                simpleAnimating = false;
                detach(p);

                // piccola spinta in uscita
                double boost = Math.max(180.0, Math.min(360.0, speed));
                p.setVelocityX(boost * (dir == -1 ? +1 : -1));
                p.setVelocityY(40.0);
            }
            return;
        }
    }
}
