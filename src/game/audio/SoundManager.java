package game.audio;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

import static game.util.ResourceUtil.clip;

public final class SoundManager {
    public enum Kind { MUSIC, SFX }

    private static final Map<String, AudioClip> CACHE = new HashMap<>();
    private static AudioClip currentMusic = null;
    private static String currentMusicKey = null;

    private static double master = 1.0;
    private static double music = 0.7;
    private static double sfx   = 1.0;

    private SoundManager() {}

    private static AudioClip get(String classpath) {
        return CACHE.computeIfAbsent(classpath, k -> clip(k));
    }

    public static void setMaster(double v) { master = clamp01(v); refreshVolumes(); }
    public static void setMusic(double v)  { music  = clamp01(v); refreshVolumes(); }
    public static void setSfx(double v)    { sfx    = clamp01(v); }

    public static double getMaster() { return master; }
    public static double getMusic()  { return music; }
    public static double getSfx()    { return sfx; }

    private static double clamp01(double v){ return v < 0 ? 0 : (v > 1 ? 1 : v); }

    private static void refreshVolumes() {
        if (currentMusic != null) currentMusic.setVolume(master * music);
    }

    public static void playMusic(String classpath) {
        if (classpath != null && classpath.equals(currentMusicKey) && currentMusic != null) return;
        stopMusic();
        currentMusicKey = classpath;
        currentMusic = get(classpath);
        currentMusic.setCycleCount(AudioClip.INDEFINITE);
        currentMusic.setVolume(master * music);
        currentMusic.play();
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
            currentMusicKey = null;
        }
    }

    public static void playSfx(String classpath) {
        AudioClip c = get(classpath);
        c.setVolume(master * sfx);
        c.play();
    }
}
