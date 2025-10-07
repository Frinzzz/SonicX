package game.audio;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;
import static game.util.ResourceUtil.clip;

/** Simple audio manager with caching and music handoff. */
public final class SoundManager {

    public enum Kind { MUSIC, SFX }

    private static final Map<String, AudioClip> CACHE = new HashMap<>();
    private static AudioClip currentMusic = null;
    private static String currentMusicKey = null;

    private static double master = 1.0;
    private static double music = 0.7;
    private static double sfx = 1.0;

    private SoundManager() {}

    private static AudioClip get(String classpath) {
        return CACHE.computeIfAbsent(classpath, cp -> {
            try {
                return clip(cp);
            } catch (Exception ex1) {
                try {
                    // Fallback: direct URL/string such as file:...
                    return new AudioClip(cp);
                } catch (Exception ex2) {
                    System.err.println("[SoundManager] Failed to load: " + cp + " -> " + ex1 + " / " + ex2);
                    return null;
                }
            }
        });
    }

    public static void setMaster(double v) { master = clamp01(v); applyVolumes(); }
    public static void setMusic(double v)  { music  = clamp01(v); applyVolumes(); }
    public static void setSfx(double v)    { sfx    = clamp01(v); }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    private static void applyVolumes() {
        if (currentMusic != null) currentMusic.setVolume(master * music);
    }

    /** Plays looping background music for a given classpath resource. */
    public static void playMusic(String classpath) {
        if (classpath == null || classpath.isEmpty()) return;
        if (classpath.equals(currentMusicKey) && currentMusic != null) {
            // already playing this track
            return;
        }
        stopMusic();
        AudioClip ac = get(classpath);
        if (ac != null) {
            currentMusic = ac;
            currentMusicKey = classpath;
            try { ac.stop(); } catch (Throwable ignored) {}
            ac.setCycleCount(AudioClip.INDEFINITE); // loop infinito
            ac.setVolume(master * music);
            ac.play();
        }
    }

    /** Stops any currently playing music. */
    public static void stopMusic() {
        if (currentMusic != null) {
            try { currentMusic.stop(); } catch (Throwable ignored) {}
            currentMusic = null;
            currentMusicKey = null;
        }
    }

    /** Plays a one-shot sound effect. */
    public static void playSfx(String classpath) {
        AudioClip ac = get(classpath);
        if (ac != null) {
            ac.setCycleCount(1);
            ac.setVolume(master * sfx);
            ac.play();
        }
    }

    /** Free audio resources. */
    public static void dispose() {
        stopMusic();
        for (AudioClip ac : CACHE.values()) {
            try { ac.stop(); } catch (Throwable ignored) {}
        }
        CACHE.clear();
    }
}
