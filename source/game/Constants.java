package game;


import static game.util.ResourceUtil.*;
/** Centralized tunables to avoid magic numbers. */
public final class Constants {
    private Constants() {}

    // Physics
    public static final double GRAVITY = 1400.0;
    public static final double PLAYER_SPEED = 280.0;
    public static final double PLAYER_JUMP_SPEED = 520.0;

    // Lives
    public static final int PLAYER_LIVES = 3;
    public static final int BOSS_LIVES = 3;

    // Boss
    public static final double BOSS_MOVE_SPEED = 180.0;
    public static final double BOSS_JUMP_SPEED = 360.0;
    public static final double BOSS_DASH_SPEED = 520.0;

    // Timing
    public static final double MAX_DT = 1.0 / 30.0; // clamp to avoid tunneling on lags

    // Camera
    public static final double CAMERA_LERP = 10.0;
}
