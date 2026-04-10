package cvccontroller;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Maps held ItemStacks to their WeaponCategory for the CvC minigame.
 *
 * Item → Weapon mapping (enchanted and unenchanted unless noted):
 *
 * PISTOLS:
 *   USP    — wooden pickaxe, iron pickaxe
 *   HK     — stone pickaxe, wooden shovel
 *   Magnum — gold pickaxe, diamond pickaxe
 *
 * SHOTGUNS:
 *   Pump   — diamond shovel, wooden hoe
 *   Spas   — wooden axe, stone axe
 *
 * SMGs:
 *   MP5    — stone shovel, iron shovel
 *   P50    — gold shovel, iron hoe
 *
 * RIFLES:
 *   AK47   — stone hoe, diamond hoe
 *   M4     — iron axe, diamond axe
 *
 * SCOPED RIFLES:
 *   Aug    — gold axe, gold hoe
 *
 * SNIPERS:
 *   50cal  — bow (unenchanted only)
 *
 * Everything else (knives, utilities, etc.) returns NONE → default controls.
 */
public class WeaponRegistry {

    public static WeaponCategory getCategory(ItemStack stack) {
        if (stack == null) return WeaponCategory.NONE;

        Item item = stack.getItem();

        // ── PISTOLS ────────────────────────────────────────────────────────────
        // USP: wooden pickaxe, iron pickaxe
        // HK:  stone pickaxe,  wooden shovel
        // Magnum: gold pickaxe, diamond pickaxe
        if (item == Items.wooden_pickaxe
                || item == Items.iron_pickaxe
                || item == Items.stone_pickaxe
                || item == Items.wooden_shovel
                || item == Items.golden_pickaxe
                || item == Items.diamond_pickaxe) {
            return WeaponCategory.PISTOL;
        }

        // ── SHOTGUNS ───────────────────────────────────────────────────────────
        // Pump: diamond shovel, wooden hoe
        // Spas: wooden axe,    stone axe
        if (item == Items.diamond_shovel
                || item == Items.wooden_hoe
                || item == Items.wooden_axe
                || item == Items.stone_axe) {
            return WeaponCategory.SHOTGUN;
        }

        // ── SMGs ───────────────────────────────────────────────────────────────
        // MP5: stone shovel, iron shovel
        // P50: gold shovel,  iron hoe
        if (item == Items.stone_shovel
                || item == Items.iron_shovel
                || item == Items.golden_shovel
                || item == Items.iron_hoe) {
            return WeaponCategory.SMG;
        }

        // ── RIFLES ─────────────────────────────────────────────────────────────
        // AK47: stone hoe,   diamond hoe
        // M4:   iron axe,    diamond axe
        if (item == Items.stone_hoe
                || item == Items.diamond_hoe
                || item == Items.iron_axe
                || item == Items.diamond_axe) {
            return WeaponCategory.RIFLE;
        }

        // ── SCOPED RIFLES ──────────────────────────────────────────────────────
        // Aug: gold axe, gold hoe
        if (item == Items.golden_axe
                || item == Items.golden_hoe) {
            return WeaponCategory.SCOPED;
        }

        // ── SNIPERS ────────────────────────────────────────────────────────────
        // 50cal: bow (unenchanted only — enchanted bow is not this weapon)
        if (item == Items.bow && !stack.isItemEnchanted()) {
            return WeaponCategory.SNIPER;
        }

        // Knives, utilities, and everything else use default controls.
        return WeaponCategory.NONE;
    }
}
