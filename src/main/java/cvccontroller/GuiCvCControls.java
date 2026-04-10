package cvccontroller;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Custom key-binding configuration screen for CvC Controller Mod.
 *
 * Opens when the player presses the "Open Settings" key (default: K).
 * Controls stored here are completely separate from Minecraft's built-in
 * Controls menu, so there are no key-conflict warnings.
 *
 * Layout:
 *   ┌──────────────────────────────────────┐
 *   │      CvC Controller Settings         │
 *   ├──────────────────────────────────────┤
 *   │  Shoot     (all guns)  [ Left Click ]│
 *   │  Reload    (all guns)  [ R          ]│
 *   │  Aim (Aug / 50cal only)[ Right Click]│
 *   │  Open Settings         [ K          ]│
 *   ├──────────────────────────────────────┤
 *   │     [Reset Defaults]   [   Done   ]  │
 *   └──────────────────────────────────────┘
 *
 * Click any binding button to enter "listening" mode — the next key or
 * mouse click becomes the new binding.  Press ESC to cancel.
 */
public class GuiCvCControls extends GuiScreen {

    // Button IDs
    private static final int ID_SHOOT    = 1;
    private static final int ID_RELOAD   = 2;
    private static final int ID_AIM      = 3;
    private static final int ID_OPEN_GUI = 4;
    private static final int ID_RESET    = 5;
    private static final int ID_DONE     = 6;

    private static final int BTN_W  = 130;
    private static final int BTN_H  = 20;
    private static final int ROW_H  = 28;   // vertical spacing between rows
    private static final int PANEL_W = 380;
    private static final int PANEL_H = 215;

    /** Which binding button is currently waiting for a key press (-1 = none). */
    private int listeningId = -1;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void initGui() {
        listeningId = -1;
        buttonList.clear();

        int cx = width  / 2;
        int cy = height / 2;

        // Binding buttons — right column
        int bx = cx + 10;
        int by = cy - 68;

        buttonList.add(new GuiButton(ID_SHOOT,    bx, by,              BTN_W, BTN_H, label(ID_SHOOT)));
        buttonList.add(new GuiButton(ID_RELOAD,   bx, by + ROW_H,      BTN_W, BTN_H, label(ID_RELOAD)));
        buttonList.add(new GuiButton(ID_AIM,      bx, by + ROW_H * 2,  BTN_W, BTN_H, label(ID_AIM)));
        buttonList.add(new GuiButton(ID_OPEN_GUI, bx, by + ROW_H * 3,  BTN_W, BTN_H, label(ID_OPEN_GUI)));

        // Bottom row
        buttonList.add(new GuiButton(ID_RESET, cx - 135, cy + 78, 125, BTN_H, "Reset Defaults"));
        buttonList.add(new GuiButton(ID_DONE,  cx +  10, cy + 78, 125, BTN_H, "Done"));
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int cx = width  / 2;
        int cy = height / 2;

        // ── Panel ─────────────────────────────────────────────────────────────
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2 - 5;

        // Outer shadow
        drawRect(px + 3, py + 3, px + PANEL_W + 3, py + PANEL_H + 3, 0x55000000);
        // Panel body
        drawGradientRect(px, py, px + PANEL_W, py + PANEL_H, 0xE0141428, 0xE01C1C3A);
        // Top accent bar
        drawRect(px, py, px + PANEL_W, py + 3, 0xFF4466CC);
        // Separator under title
        drawRect(px + 12, py + 26, px + PANEL_W - 12, py + 27, 0x664466CC);
        // Separator above bottom buttons
        drawRect(px + 12, py + PANEL_H - 38, px + PANEL_W - 12, py + PANEL_H - 37, 0x334466CC);

        // ── Title ─────────────────────────────────────────────────────────────
        drawCenteredString(fontRendererObj,
            "\u00a7b\u00a7lCvC Controller Settings",
            cx, py + 9, 0xFFFFFF);

        // ── Row labels (left column) ──────────────────────────────────────────
        int lx  = px + 18;
        int by0 = cy - 62;

        drawString(fontRendererObj,
            "\u00a7fShoot  \u00a78\u00a7o(all weapons)",
            lx, by0, 0xFFFFFF);

        drawString(fontRendererObj,
            "\u00a7fReload \u00a78\u00a7o(all weapons)",
            lx, by0 + ROW_H, 0xFFFFFF);

        drawString(fontRendererObj,
            "\u00a73Aim    \u00a78\u00a7o(Aug & 50cal only)",
            lx, by0 + ROW_H * 2, 0xFFFFFF);

        drawString(fontRendererObj,
            "\u00a77Open Settings",
            lx, by0 + ROW_H * 3, 0xFFFFFF);

        // ── Status hint ───────────────────────────────────────────────────────
        String hint = listeningId != -1
            ? "\u00a7ePress any key or mouse button \u00a77(\u00a7cESC\u00a77 to cancel)"
            : "\u00a77Click a button to rebind it";
        drawCenteredString(fontRendererObj, hint, cx, cy + 62, 0xFFFFFF);

        // ── Render buttons on top ─────────────────────────────────────────────
        // Highlight listening button (change displayString live)
        refreshButtonLabels();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case ID_DONE:
                mc.displayGuiScreen(null);
                return;

            case ID_RESET:
                ControlConfig.resetDefaults();
                listeningId = -1;
                refreshButtonLabels();
                return;

            case ID_SHOOT:
            case ID_RELOAD:
            case ID_AIM:
            case ID_OPEN_GUI:
                // Toggle listening — clicking same button again cancels
                listeningId = (listeningId == button.id) ? -1 : button.id;
                refreshButtonLabels();
                return;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (listeningId != -1) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                listeningId = -1;   // cancel
            } else {
                applyBinding(keyCode);
            }
            refreshButtonLabels();
            return;
        }
        // Allow ESC to close the screen normally when not listening
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (listeningId != -1) {
            // mouseButton: 0=left, 1=right, 2=middle
            // Minecraft encoding:  Left=-100, Right=-99, Middle=-98
            applyBinding(mouseButton - 100);
            refreshButtonLabels();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Applies a key code to whichever binding is currently listening. */
    private void applyBinding(int keyCode) {
        switch (listeningId) {
            case ID_SHOOT:    ControlConfig.shootKey   = keyCode; break;
            case ID_RELOAD:   ControlConfig.reloadKey  = keyCode; break;
            case ID_AIM:      ControlConfig.aimKey     = keyCode; break;
            case ID_OPEN_GUI: ControlConfig.openGuiKey = keyCode; break;
        }
        ControlConfig.save();
        listeningId = -1;
    }

    /** Returns the label text for a binding button. */
    private String label(int btnId) {
        if (btnId == listeningId) return "\u00a7e> Press any key <";
        switch (btnId) {
            case ID_SHOOT:    return ControlConfig.getKeyName(ControlConfig.shootKey);
            case ID_RELOAD:   return ControlConfig.getKeyName(ControlConfig.reloadKey);
            case ID_AIM:      return ControlConfig.getKeyName(ControlConfig.aimKey);
            case ID_OPEN_GUI: return ControlConfig.getKeyName(ControlConfig.openGuiKey);
        }
        return "?";
    }

    /** Syncs all button displayStrings with the current config / listening state. */
    private void refreshButtonLabels() {
        for (Object obj : buttonList) {
            GuiButton btn = (GuiButton) obj;
            int id = btn.id;
            if (id == ID_SHOOT || id == ID_RELOAD || id == ID_AIM || id == ID_OPEN_GUI) {
                btn.displayString = label(id);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // never pause — the menu is used on multiplayer servers
    }
}
