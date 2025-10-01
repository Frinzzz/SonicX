package game.state;

import java.util.prefs.Preferences;

public class SaveData {
    private static final Preferences PREFS = Preferences.userRoot().node("sonic_menu");

    public static double getMaster() { return PREFS.getDouble("master", 0.8); }
    public static double getMusic()  { return PREFS.getDouble("music",  0.6); }
    public static double getSfx()    { return PREFS.getDouble("sfx",    0.8); }

    public static void setMaster(double v){ PREFS.putDouble("master", clamp01(v)); }
    public static void setMusic(double v) { PREFS.putDouble("music",  clamp01(v)); }
    public static void setSfx(double v)   { PREFS.putDouble("sfx",    clamp01(v)); }

    private static double clamp01(double v){ return v < 0 ? 0 : (v > 1 ? 1 : v); }
}
