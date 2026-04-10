package cvccontroller;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CvC Controller Mod — Minecraft 1.8.9 / Forge 11.15.1.2318
 *
 * Remaps controls while the player holds a CvC weapon, without touching
 * Minecraft's built-in Controls menu (no KeyBinding registration → no conflicts).
 *
 *   Pistols / Shotguns / SMGs / Rifles:
 *     Shoot   (default Left Click) → fires Use Item  (right-click action)
 *     Reload  (default R)          → fires Attack    (left-click action)
 *
 *   Scoped Rifles (Aug) / Snipers (50cal):
 *     Shoot   (default Left Click) → fires Use Item
 *     Reload  (default R)          → fires Attack
 *     Aim     (default Right Click)→ fires Sneak     (shift action)
 *
 *   Open Settings (default K) — opens the custom key-binding GUI.
 *
 *   Knives / Utilities / everything else → default Minecraft controls.
 */
@Mod(
    modid        = CvCControllerMod.MOD_ID,
    name         = CvCControllerMod.MOD_NAME,
    version      = CvCControllerMod.VERSION,
    useMetadata  = true,
    clientSideOnly = true
)
public class CvCControllerMod {

    public static final String MOD_ID   = "cvccontrollermod";
    public static final String MOD_NAME = "CvC Controller Mod";
    public static final String VERSION  = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("[{}] Loading config...", MOD_NAME);
        ControlConfig.init(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("[{}] Registering input remapper...", MOD_NAME);
        // InputEvent (mouse/keyboard) and TickEvent fire on the FML bus in Forge 1.8.9,
        // NOT on MinecraftForge.EVENT_BUS — using the wrong bus is why handlers never fire.
        FMLCommonHandler.instance().bus().register(new InputRemapper());
        LOGGER.info("[{}] Ready. Press {} to open settings.",
            MOD_NAME, ControlConfig.getKeyName(ControlConfig.openGuiKey));
    }
}
