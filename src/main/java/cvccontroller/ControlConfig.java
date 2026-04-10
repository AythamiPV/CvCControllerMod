package cvccontroller;

import org.lwjgl.input.Keyboard;

import java.io.*;
import java.util.Properties;

/**
 * Stores and persists the four configurable key codes.
 *
 * Key codes follow Minecraft / LWJGL 2 conventions:
 *   Keyboard keys : positive int  (Keyboard.KEY_R = 19, etc.)
 *   Mouse buttons : negative int  (Left=-100, Right=-99, Middle=-98)
 *   Unbound       : 0
 */
public class ControlConfig {

    // ── Defaults ──────────────────────────────────────────────────────────────
    public static final int DEFAULT_SHOOT    = -100; // Left Mouse Button
    public static final int DEFAULT_RELOAD   =  Keyboard.KEY_R;
    public static final int DEFAULT_AIM      =  -99; // Right Mouse Button
    public static final int DEFAULT_OPEN_GUI =  Keyboard.KEY_K;

    public static int shootKey  = DEFAULT_SHOOT;
    public static int reloadKey = DEFAULT_RELOAD;
    public static int aimKey    = DEFAULT_AIM;
    public static int openGuiKey = DEFAULT_OPEN_GUI;

    private static File configFile;

    // ── Init / Load / Save ────────────────────────────────────────────────────

    public static void init(File configDir) {
        configFile = new File(configDir, "cvccontroller.cfg");
        load();
    }

    public static void load() {
        if (configFile == null || !configFile.exists()) {
            save();
            return;
        }
        Properties p = new Properties();
        try (Reader r = new FileReader(configFile)) {
            p.load(r);
            shootKey   = parseInt(p.getProperty("shoot"),    DEFAULT_SHOOT);
            reloadKey  = parseInt(p.getProperty("reload"),   DEFAULT_RELOAD);
            aimKey     = parseInt(p.getProperty("aim"),      DEFAULT_AIM);
            openGuiKey = parseInt(p.getProperty("openGui"),  DEFAULT_OPEN_GUI);
        } catch (IOException e) {
            CvCControllerMod.LOGGER.warn("[CvCControllerMod] Could not load config: {}", e.getMessage());
        }
    }

    public static void save() {
        if (configFile == null) return;
        Properties p = new Properties();
        p.setProperty("shoot",   String.valueOf(shootKey));
        p.setProperty("reload",  String.valueOf(reloadKey));
        p.setProperty("aim",     String.valueOf(aimKey));
        p.setProperty("openGui", String.valueOf(openGuiKey));
        try (Writer w = new FileWriter(configFile)) {
            p.store(w,
                "CvC Controller Mod — key bindings\n" +
                "# Keyboard keys: positive LWJGL key code (e.g. R=19, K=37)\n" +
                "# Mouse buttons: Left=-100  Right=-99  Middle=-98\n" +
                "# Unbound: 0");
        } catch (IOException e) {
            CvCControllerMod.LOGGER.warn("[CvCControllerMod] Could not save config: {}", e.getMessage());
        }
    }

    public static void resetDefaults() {
        shootKey   = DEFAULT_SHOOT;
        reloadKey  = DEFAULT_RELOAD;
        aimKey     = DEFAULT_AIM;
        openGuiKey = DEFAULT_OPEN_GUI;
        save();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /** Human-readable name for a key code (supports keyboard and mouse). */
    public static String getKeyName(int keyCode) {
        if (keyCode == 0) return "None";
        if (keyCode < 0) {
            int btn = keyCode + 100; // convert to button index
            switch (btn) {
                case 0:  return "Left Click";
                case 1:  return "Right Click";
                case 2:  return "Middle Click";
                default: return "Mouse " + (btn + 1);
            }
        }
        String name = Keyboard.getKeyName(keyCode);
        return (name != null && !name.isEmpty()) ? name : ("Key " + keyCode);
    }

    private static int parseInt(String s, int def) {
        if (s == null) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}
