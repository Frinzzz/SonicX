package game.entities;

import static game.util.ResourceUtil.*;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.geometry.Bounds;

public abstract class EnemyBase {
    protected ImageView sprite;
    protected boolean alive = true;

    public abstract void update(double dt);

    /** Danno direzionale in stile Sonic:
     *  - se il player arriva dall'alto in caduta -> stomp (nemico muore, rimbalzo)
     *  - altrimenti -> danno al player
     */
        public void onPlayerCollision(Player player) {
        if (!alive) return;

        Bounds pb = player.getBounds();
        Bounds eb = sprite.getBoundsInParent();

        // Crossing test: bottom del player attraversa la top-line del nemico
        final double enemyTop   = eb.getMinY();
        final double curBottom  = pb.getMaxY();
        final double prevBottom = player.getLastCenterY() + pb.getHeight() * 0.5;

        // Tolleranza per velocità alte / step discreti
        final double topTolerance = 4.0;
        final boolean crossedFromAbove = (prevBottom <= enemyTop + topTolerance) && (curBottom >= enemyTop - topTolerance);

        // Deve essere in discesa (ora o nel frame precedente)
        final boolean falling = player.getVelocityY() > 0 || player.getLastVelocityY() > 0;

        // Richiedi anche una sovrapposizione orizzontale minima per evitare stomp di spigolo
        final double overlapLeft  = Math.max(pb.getMinX(), eb.getMinX());
        final double overlapRight = Math.min(pb.getMaxX(), eb.getMaxX());
        final double overlapW     = Math.max(0, overlapRight - overlapLeft);
        final double minOverlap   = Math.min(eb.getWidth(), pb.getWidth()) * 0.30; // 30%

        if (crossedFromAbove && falling && overlapW >= minOverlap) {
            // STOMP: uccide il nemico e rimbalza
            alive = false;
            player.setVelocityY(-420.0);
            try { game.audio.SoundManager.playSfx("/assets/sfx/stomp.wav"); } catch (Throwable ignored) {}
            if (sprite != null && sprite.getParent() instanceof Group) {
                ((Group) sprite.getParent()).getChildren().remove(sprite);
            }
        } else if (player.isRolling()) {
            // In roll/spindash: uccidi il nemico anche con impatto laterale
            alive = false;
            try { game.audio.SoundManager.playSfx("/assets/sfx/enemy_die.wav"); } catch (Throwable ignored) {}
            if (sprite != null && sprite.getParent() instanceof Group) {
                ((Group) sprite.getParent()).getChildren().remove(sprite);
            }
        } else {
            // Colpo laterale -> danno al player (gestito con invuln interna del player)
            player.applyKnockback((getBounds().getMinX()+getBounds().getMaxX())*0.5);
            player.takeDamage();
        }
    }


    public boolean isAlive() { return alive; }
    public ImageView getSprite() { return sprite; }
    public Bounds getBounds() { return sprite.getBoundsInParent(); }
}
