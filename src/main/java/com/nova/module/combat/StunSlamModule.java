package com.nova.module.combat;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import com.nova.util.InvUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/**
 * Stun Slam: Du hältst die Mace und schlägst zu. Die Mod haut davor (im selben Tick, ohne dass du switchst)
 * mit der Axt (Gegner hält Schild) bzw. optional mit dem Schwert und danach schlägt dein normaler Mace-Hit zu.
 * Delay 0 = Zero Tick. Mit Delay 1-4 kommt der Mace-Hit entsprechend später (wirkt menschlicher).
 */
public class StunSlamModule extends Module {
    private static boolean busy;

    private final NumSetting delay = num("Delay (Ticks)", 0, 0, 4, 1);
    private final BoolSetting onlyFalling = bool("Nur im Fall", true);
    private final BoolSetting swordCombo = bool("Schwert wenn kein Schild", true);

    private LivingEntity pendingTarget;
    private int pendingSlot;
    private int pendingTicks;

    public StunSlamModule() {
        super("Stun Slam", "Axt (Schild weg) + Mace im selben Tick. Einfach mit Mace auf den Gegner springen.", Category.COMBAT);
    }

    /** Wird vom AttackEntityCallback aufgerufen. */
    public ActionResult onAttack(Entity entity) {
        if (busy || !(entity instanceof LivingEntity target)) return ActionResult.PASS;
        ClientPlayerEntity p = mc.player;
        if (p == null || !p.getMainHandStack().isOf(Items.MACE)) return ActionResult.PASS;
        if (onlyFalling.get() && !p.isGliding() && (p.isOnGround() || p.getVelocity().y > -0.08)) return ActionResult.PASS;

        int pre = pickPre(target, swordCombo.get());
        if (pre < 0) return ActionResult.PASS;

        int maceSlot = InvUtil.selected();
        hit(pre, target);
        InvUtil.select(maceSlot);

        if (delay.getInt() <= 0) return ActionResult.PASS; // Mace-Hit läuft direkt danach, Zero Tick

        pendingTarget = target;
        pendingSlot = maceSlot;
        pendingTicks = delay.getInt();
        return ActionResult.FAIL; // ursprünglichen Hit verschieben
    }

    @Override
    public void onTick() {
        if (pendingTarget == null) return;
        if (--pendingTicks > 0) return;
        LivingEntity t = pendingTarget;
        pendingTarget = null;
        if (t.isAlive() && mc.player.distanceTo(t) <= 4.5f) {
            hit(pendingSlot, t);
        }
    }

    @Override
    public void onDisable() {
        pendingTarget = null;
    }

    /** Wählt Axt (wenn Gegner blockt) bzw. Schwert (optional). -1 = nichts passendes. */
    public static int pickPre(LivingEntity target, boolean swordFallback) {
        if (target.isBlocking()) {
            return InvUtil.findHotbar(s -> s.isIn(ItemTags.AXES));
        }
        if (swordFallback) {
            return InvUtil.findHotbar(s -> s.isIn(ItemTags.SWORDS));
        }
        return -1;
    }

    /** Wechselt auf den Slot, schlägt zu, ohne dass der Callback wieder anspringt. */
    public static void hit(int slot, Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        busy = true;
        try {
            InvUtil.select(slot);
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        } finally {
            busy = false;
        }
    }
}
