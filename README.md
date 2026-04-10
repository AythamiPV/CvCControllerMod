# 🎯 CvCControllerMod

> **Remaps CvC controls on-the-fly — shoot with Left Click, reload with R, aim with Right Click.**
> No keybinding conflicts. No restarts. Just play.

---

## 📋 Overview

**CvCControllerMod** is a client-side Forge mod for **Minecraft 1.8.9** designed specifically for the **Cops vs Crims (CvC)** minigame on Podcrash Play.

By default, CvC uses counterintuitive controls inherited from vanilla Minecraft: Right Click shoots, Left Click reloads. This mod silently intercepts the raw input events and remaps them to a more natural FPS layout — without touching Minecraft's built-in Controls menu and without causing any key conflicts.

---

## 🕹️ Default Control Remapping

### Pistols · Shotguns · SMGs · Rifles
| Action | Default (CvC) | With Mod |
|--------|--------------|----------|
| Shoot  | Right Click  | **Left Click** |
| Reload | Left Click   | **R** |

### Scoped Rifles (Aug) · Snipers (50cal)
| Action | Default (CvC) | With Mod |
|--------|--------------|----------|
| Shoot  | Right Click  | **Left Click** |
| Reload | Left Click   | **R** |
| Aim    | Left Shift   | **Right Click** |

### Knives & Utilities
No changes — default CvC controls apply as normal.

---

## 🔫 Weapon Detection

The mod identifies the weapon you're holding by its item type and automatically applies the correct control scheme:

| Category | Weapons | Items |
|----------|---------|-------|
| **Pistols** | USP, HK, Magnum | Wooden/Iron/Stone/Gold/Diamond Pickaxe, Wooden Shovel |
| **Shotguns** | Pump, Spas | Diamond Shovel, Wooden Hoe, Wooden/Stone Axe |
| **SMGs** | MP5, P50 | Stone/Iron/Gold Shovel, Iron Hoe |
| **Rifles** | AK47, M4 | Stone/Diamond Hoe, Iron/Diamond Axe |
| **Scoped** | Aug | Gold Axe, Gold Hoe |
| **Sniper** | 50cal | Bow (unenchanted) |
| **Knives / Utilities** | — | Everything else |

---

## ⚙️ Configuration

Press **K** (default) in-game to open the **CvC Controller Settings** screen.

From there you can rebind:
- **Shoot** — which key/button fires the weapon (default: Left Click)
- **Reload** — which key triggers reload (default: R)
- **Aim** — which key/button scopes in for Aug & 50cal (default: Right Click)
- **Open Settings** — which key opens this menu (default: K)

Click any binding button, then press a key or mouse button to reassign it. Press **ESC** to cancel a rebind. All settings are saved automatically to `config/cvccontroller.cfg`.

---

## 🛠️ Technical Details

| Property | Value |
|----------|-------|
| Minecraft version | **1.8.9** |
| Mod loader | **Forge 11.15.1.2318** |
| Side | **Client-only** |
| No server-side install needed | ✅ |
| Compatible with Podcrash Play | ✅ |

The mod works by registering a `MouseEvent` and `KeyInputEvent` listener at **HIGHEST priority** on the Forge event bus. When the player holds a remapped weapon:
- **Left Click** events are intercepted and replaced with a `rightClickMouse()` call (which CvC reads as shoot).
- **R key** events trigger a `clickMouse()` call (which CvC reads as reload).
- **Right Click** is passed through for SCOPED/SNIPER categories (aim), and blocked for all others.

This approach requires no reflection on obfuscated fields and produces zero latency — the interception happens at the same tick as the input.

---

## 📁 File Structure

```
CvCControllerMod/
├── src/main/java/cvccontroller/
│   ├── CvCControllerMod.java     — Mod entry point & registration
│   ├── InputRemapper.java        — Core input interception logic
│   ├── WeaponCategory.java       — Enum of weapon categories
│   ├── WeaponRegistry.java       — Item → category mapping
│   ├── ControlConfig.java        — Config load/save & key names
│   └── GuiCvCControls.java       — In-game settings screen
└── src/main/resources/
    └── mcmod.info
```

---

## ⬇️ Download

[![Download](https://img.shields.io/badge/⬇️_Download-v1.0.0-2ea44f?style=for-the-badge&logo=github)](https://github.com/AythamiPV/CvCControllerMod/releases/download/v1.0.0/cvccontrollermod-1.0.0.jar)

Or go to the [Releases page](https://github.com/AythamiPV/CvCControllerMod/releases/latest).

---

## 🚀 Installation

1. Make sure you have **Forge 11.15.1.2318** for Minecraft 1.8.9 installed.
2. Drop `CvCControllerMod-1.0.0.jar` into your `.minecraft/mods/` folder.
3. Launch the game and join Cops vs Crims.
4. Controls are remapped automatically. Press **K** to customize.

---

## 📝 Notes

- The mod is **client-side only** — it will appear as a "missing mod" on the server connection screen, which is completely normal and expected.
- Settings reset to defaults via the **Reset Defaults** button in the settings GUI.
- If a key binding conflicts with another mod, use the settings screen to reassign it.

---

*CvCControllerMod v1.0.0 — Made for Minecraft 1.8.9 / Forge 11.15.1.2318*
