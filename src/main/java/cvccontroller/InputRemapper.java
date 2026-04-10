package cvccontroller;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Method;

/**
 * Intercepts physical input via Forge events ({@link EventPriority#HIGHEST}) and
 * redirects it before CvC processes it.
 *
 * <p><b>All weapon categories:</b>
 * <ul>
 *   <li>Left Click  &rarr; shoot  (CvC: Right Click via {@code rightClickMouse})</li>
 *   <li>R key       &rarr; reload (CvC: Left Click  via {@code clickMouse})</li>
 * </ul>
 *
 * <p><b>SCOPED / SNIPER only:</b>
 * <ul>
 *   <li>Right Click hold    &rarr; aim (CvC: Left Shift / sneak)</li>
 *   <li>Right Click release &rarr; stop aiming</li>
 * </ul>
 *
 * <p>Aiming is implemented by sending {@link C0BPacketEntityAction#START_SNEAKING} and
 * {@link C0BPacketEntityAction#STOP_SNEAKING} packets directly to the server, without
 * setting {@code movementInput.sneak} on the client. This means CvC detects the sneak
 * state server-side (activating aim) while the client never enters sneak mode, so
 * movement speed is not affected by the vanilla sneak penalty.
 */
public class InputRemapper {

    private static final boolean DEBUG = true;

    /** {@code true} while the player holds Right Click to aim (SCOPED/SNIPER only). */
    private boolean aimHeld = false;

    /**
     * {@code true} after a {@code START_SNEAKING} packet has been sent for the current
     * aim session. Used to send a matching {@code STOP_SNEAKING} if the player switches
     * weapons while aiming.
     */
    private boolean sneakPacketSent = false;

    // ── Reflection ────────────────────────────────────────────────────────────
    //
    // rightClickMouse() and clickMouse() are private in Minecraft 1.8.9.
    // We resolve them at class-load time by trying both the MCP deobfuscated
    // name and the SRG name, so the mod works in both dev and production.

    private static final Method METHOD_RIGHT_CLICK_MOUSE;
    private static final Method METHOD_CLICK_MOUSE;

    static {
        METHOD_RIGHT_CLICK_MOUSE = findMethod(Minecraft.class,
            "rightClickMouse", "func_147121_ag");
        METHOD_CLICK_MOUSE = findMethod(Minecraft.class,
            "clickMouse",      "func_147116_af");
    }

    /**
     * Attempts to find a declared method on {@code clazz} by trying each name in order.
     * The first match is made accessible and returned; {@code null} is returned if none
     * of the names resolve.
     *
     * @param clazz the class to search
     * @param names candidate method names (MCP name first, SRG name second)
     * @return the resolved {@link Method}, or {@code null}
     */
    private static Method findMethod(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                Method m = clazz.getDeclaredMethod(name);
                m.setAccessible(true);
                CvCControllerMod.LOGGER.info("[CvCControllerMod] Method resolved: {}", name);
                return m;
            } catch (NoSuchMethodException ignored) {}
        }
        CvCControllerMod.LOGGER.error("[CvCControllerMod] FATAL: could not resolve method: {}",
            java.util.Arrays.toString(names));
        return null;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    /**
     * Monitors weapon changes during an active aim session. If the player switches to a
     * weapon that is not {@link WeaponCategory#SCOPED} or {@link WeaponCategory#SNIPER}
     * while {@link #aimHeld} is {@code true}, a {@code STOP_SNEAKING} packet is sent
     * immediately so the server does not remain stuck in sneak/aim state.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null || mc.getNetHandler() == null) {
            if (sneakPacketSent) {
                aimHeld         = false;
                sneakPacketSent = false;
            }
            return;
        }

        if (aimHeld) {
            WeaponCategory cat = getCurrentCategory(mc);
            boolean stillScoped = cat == WeaponCategory.SCOPED || cat == WeaponCategory.SNIPER;
            if (!stillScoped) {
                aimHeld = false;
                sendSneak(mc, false);
            }
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    /**
     * Intercepts mouse button events at {@link EventPriority#HIGHEST} priority.
     *
     * <ul>
     *   <li><b>Shoot button (default Left Click):</b> cancelled; {@code rightClickMouse()}
     *       is invoked directly so CvC receives the Right Click action it expects for
     *       firing.</li>
     *   <li><b>Right Click on SCOPED/SNIPER:</b> cancelled; a sneak packet is sent to
     *       the server to activate or deactivate aim without touching local movement
     *       input.</li>
     *   <li><b>Right Click on other categories:</b> cancelled without any further
     *       action, preventing unintended vanilla interactions.</li>
     * </ul>
     *
     * @param event the Forge mouse event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouse(MouseEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;
        if (event.button < 0) return; // scroll-wheel event; button == -1

        WeaponCategory cat = getCurrentCategory(mc);
        if (cat == WeaponCategory.NONE) return;

        if (DEBUG) {
            CvCControllerMod.LOGGER.info(
                "[CvCControllerMod] MouseEvent btn={} state={} cat={}",
                event.button, event.buttonstate, cat);
        }

        int shootBtn = toPhysicalButton(ControlConfig.shootKey); // e.g. -100 → 0
        boolean isScoped = cat == WeaponCategory.SCOPED || cat == WeaponCategory.SNIPER;

        if (event.button == shootBtn) {
            event.setCanceled(true);
            if (event.buttonstate) {
                invokePrivate(mc, METHOD_RIGHT_CLICK_MOUSE, "rightClickMouse");
            }

        } else if (event.button == 1) {
            event.setCanceled(true);

            if (isScoped) {
                aimHeld = event.buttonstate;
                sendSneak(mc, aimHeld);
            }
        }
    }

    // ── Keyboard ──────────────────────────────────────────────────────────────

    /**
     * Intercepts keyboard events at {@link EventPriority#HIGHEST} priority.
     *
     * <ul>
     *   <li><b>Open-GUI key (default K):</b> opens the {@link GuiCvCControls} screen.</li>
     *   <li><b>Reload key (default R):</b> invokes {@code clickMouse()} so CvC receives
     *       the Left Click action it expects for reloading.</li>
     * </ul>
     *
     * @param event the Forge key-input event
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mc.currentScreen == null
                && ControlConfig.openGuiKey > 0
                && Keyboard.getEventKey() == ControlConfig.openGuiKey
                && Keyboard.getEventKeyState()) {
            mc.displayGuiScreen(new GuiCvCControls());
            return;
        }

        if (mc.currentScreen != null) return;

        WeaponCategory cat = getCurrentCategory(mc);
        if (cat == WeaponCategory.NONE) return;

        if (ControlConfig.reloadKey > 0
                && Keyboard.getEventKey() == ControlConfig.reloadKey
                && Keyboard.getEventKeyState()) {

            if (DEBUG) {
                CvCControllerMod.LOGGER.info(
                    "[CvCControllerMod] ReloadKey → clickMouse() cat={}", cat);
            }

            invokePrivate(mc, METHOD_CLICK_MOUSE, "clickMouse");
        }
    }

    // ── Sneak packet ──────────────────────────────────────────────────────────

    /**
     * Sends a {@link C0BPacketEntityAction} sneak packet directly to the server.
     *
     * <p>This approach is preferred over setting {@code movementInput.sneak} because it
     * keeps the client out of vanilla sneak mode (no movement speed penalty), while the
     * server still observes the expected sneak state that CvC uses to detect aiming.
     *
     * @param mc           the current {@link Minecraft} instance
     * @param startSneaking {@code true} to send {@code START_SNEAKING},
     *                      {@code false} to send {@code STOP_SNEAKING}
     */
    private void sendSneak(Minecraft mc, boolean startSneaking) {
        if (mc.getNetHandler() == null) return;

        C0BPacketEntityAction.Action action = startSneaking
            ? C0BPacketEntityAction.Action.START_SNEAKING
            : C0BPacketEntityAction.Action.STOP_SNEAKING;

        mc.getNetHandler().addToSendQueue(
            new C0BPacketEntityAction(mc.thePlayer, action));

        sneakPacketSent = startSneaking;

        if (DEBUG) {
            CvCControllerMod.LOGGER.info(
                "[CvCControllerMod] C0BPacketEntityAction sent: {}", action);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Invokes a private {@link Minecraft} method via reflection, logging an error if
     * the method reference is {@code null} or the invocation fails.
     *
     * @param mc     the {@link Minecraft} instance to invoke on
     * @param method the resolved method (may be {@code null} if resolution failed)
     * @param name   human-readable method name used in error messages
     */
    private static void invokePrivate(Minecraft mc, Method method, String name) {
        if (method == null) {
            CvCControllerMod.LOGGER.error("[CvCControllerMod] {} not available", name);
            return;
        }
        try {
            method.invoke(mc);
        } catch (Exception e) {
            CvCControllerMod.LOGGER.error(
                "[CvCControllerMod] Failed to invoke {}: {}", name, e.getMessage());
        }
    }

    /**
     * Returns the held item's {@link WeaponCategory}, or {@link WeaponCategory#NONE} if
     * the player's hand is empty.
     *
     * @param mc the current {@link Minecraft} instance
     * @return the weapon category of the currently held item
     */
    private WeaponCategory getCurrentCategory(Minecraft mc) {
        ItemStack held = mc.thePlayer.getHeldItem();
        return WeaponRegistry.getCategory(held);
    }

    /**
     * Converts a {@link ControlConfig} key code to an LWJGL mouse button index.
     * Minecraft encodes mouse buttons as negative integers: Left = -100, Right = -99,
     * Middle = -98. This method maps them back to LWJGL indices (0, 1, 2, …).
     *
     * @param keyCode a key code in Minecraft / ControlConfig convention
     * @return the LWJGL button index, or {@code -1} if {@code keyCode} is a keyboard key
     */
    private static int toPhysicalButton(int keyCode) {
        return keyCode < 0 ? keyCode + 100 : -1;
    }

    /**
     * Reads the physical state of a key or mouse button directly from LWJGL, bypassing
     * Minecraft's {@link net.minecraft.client.settings.KeyBinding} abstraction.
     *
     * @param keyCode a key code in Minecraft / ControlConfig convention
     * @return {@code true} if the key or button is currently held down
     */
    static boolean isPhysicallyDown(int keyCode) {
        if (keyCode == 0) return false;
        if (keyCode < 0) {
            int btn = keyCode + 100;
            return btn >= 0 && Mouse.isButtonDown(btn);
        }
        return Keyboard.isKeyDown(keyCode);
    }
}
