package cvccontroller;

/**
 * Weapon categories for the CvC minigame.
 *
 * GUN categories (PISTOL, SHOTGUN, SMG, RIFLE):
 *   Left Click  -> Shoot  (remapped to Use Item / Right Click internally)
 *   R           -> Reload (remapped to Attack  / Left Click internally)
 *
 * SCOPED categories (SCOPED, SNIPER):
 *   Left Click  -> Shoot  (remapped to Use Item / Right Click internally)
 *   R           -> Reload (remapped to Attack  / Left Click internally)
 *   Right Click -> Aim    (remapped to Sneak   / Left Shift internally)
 *
 * NONE: default Minecraft controls, no remapping.
 */
public enum WeaponCategory {
    PISTOL,   // USP, HK, Magnum
    SHOTGUN,  // Pump, Spas
    SMG,      // MP5, P50
    RIFLE,    // AK47, M4
    SCOPED,   // Aug
    SNIPER,   // 50cal
    NONE      // Knives, Utilities, everything else — default controls
}
