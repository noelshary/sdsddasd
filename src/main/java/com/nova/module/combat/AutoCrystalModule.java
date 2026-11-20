package com.nova.module.combat;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import com.nova.util.InvUtil;
import com.nova.util.TargetUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Fast / Auto Crystal.
 * - hört sofort auf, sobald kein lebendes Ziel mehr da ist
 * - setzt Crystals nur, wenn das Ziel in der Luft ist und höher steht als du (einstellbar)
 * - schlägt das Ziel in der Luft automatisch immer wieder
 */
public class AutoCrystalModule extends Module {
    private final NumSetting range = num("Ziel-Reichweite", 8, 3, 12, 0.5);
    private final NumSetting placeDelay = num("Place Delay (Ticks)", 0, 0, 10, 1);
    private final NumSetting breakDelay = num("Break Delay (Ticks)", 0, 0, 10, 1);
    private final BoolSetting onlyAir = bool("Nur Ziel in der Luft", true);
    private final NumSetting minHeight = num("Min. Ziel-Höhe über mir", 1, -1, 5, 0.5);
    private final BoolSetting hitPlayer = bool("Spieler mitschlagen", true);
    private final NumSetting safeDist = num("Sicherheitsabstand", 2.5, 0, 6, 0.5);
    private final NumSetting minHp = num("Min. HP", 4, 0, 20, 1);

    private int placeCd, breakCd;

    public AutoCrystalModule() {
        super("Fast Crystal", "Auto Crystal: Obsidian + Crystal gegen Ziele in der Luft, stoppt bei Tod des Ziels.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (placeCd > 0) placeCd--;
        if (breakCd > 0) breakCd--;

        if (p.getHealth() + p.getAbsorptionAmount() <= minHp.get()) return;

        PlayerEntity t = TargetUtil.nearestPlayer(range.get());
        if (t == null) return; // Ziel tot oder weg -> Modul tut nichts mehr

        if (onlyAir.get() && t.isOnGround()) return;
        if (t.getY() - p.getY() < minHeight.get()) return;

        // Spieler selbst immer wieder treffen, solange er in der Luft ist
        if (hitPlayer.get() && p.distanceTo(t) <= 3.0f && p.getAttackCooldownProgress(0.5f) >= 0.95f) {
            int sword = InvUtil.findHotbar(s -> s.isIn(ItemTags.SWORDS));
            if (sword >= 0) InvUtil.select(sword);
            mc.interactionManager.attackEntity(p, t);
            p.swingHand(Hand.MAIN_HAND);
        }

        // Crystal kaputt machen
        EndCrystalEntity crystal = findCrystal(t);
        if (crystal != null && breakCd <= 0) {
            mc.interactionManager.attackEntity(p, crystal);
            p.swingHand(Hand.MAIN_HAND);
            breakCd = breakDelay.getInt();
        }

        // Neuen Crystal setzen
        if (placeCd <= 0) {
            int cs = InvUtil.findHotbar(Items.END_CRYSTAL);
            if (cs < 0) return;
            BlockPos base = findBase(t, p, w);
            if (base != null) {
                InvUtil.select(cs);
                BlockHitResult hit = new BlockHitResult(
                        new Vec3d(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5),
                        Direction.UP, base, false);
                mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
                p.swingHand(Hand.MAIN_HAND);
                placeCd = placeDelay.getInt();
            }
        }
    }

    private EndCrystalEntity findCrystal(PlayerEntity t) {
        ClientPlayerEntity p = mc.player;
        List<EndCrystalEntity> list = mc.world.getEntitiesByClass(EndCrystalEntity.class, t.getBoundingBox().expand(7), c -> true);
        EndCrystalEntity best = null;
        double bestD = Double.MAX_VALUE;
        double safe = safeDist.get() * safeDist.get();
        for (EndCrystalEntity c : list) {
            if (p.distanceTo(c) > 4.5f) continue;
            if (p.squaredDistanceTo(c) < safe) continue;
            double d = c.squaredDistanceTo(t);
            if (d < bestD) {
                bestD = d;
                best = c;
            }
        }
        return best;
    }

    private BlockPos findBase(PlayerEntity t, ClientPlayerEntity p, ClientWorld w) {
        BlockPos tp = t.getBlockPos();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        double safe = safeDist.get() * safeDist.get();
        Vec3d eye = p.getEyePos();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -4; dy <= 0; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos b = tp.add(dx, dy, dz);
                    BlockState s = w.getBlockState(b);
                    if (!(s.isOf(Blocks.OBSIDIAN) || s.isOf(Blocks.BEDROCK))) continue;
                    if (!w.getBlockState(b.up()).isAir() || !w.getBlockState(b.up(2)).isAir()) continue;

                    Vec3d top = new Vec3d(b.getX() + 0.5, b.getY() + 1.0, b.getZ() + 0.5);
                    if (eye.distanceTo(top) > 4.5) continue;
                    if (p.squaredDistanceTo(top.x, top.y, top.z) < safe) continue;

                    double d = top.squaredDistanceTo(t.getX(), t.getY(), t.getZ());
                    if (d > 16) continue;

                    Box space = new Box(b.getX(), b.getY() + 1, b.getZ(), b.getX() + 1, b.getY() + 3, b.getZ() + 1);
                    if (!w.getOtherEntities(null, space).isEmpty()) continue;

                    if (d < bestD) {
                        bestD = d;
                        best = b;
                    }
                }
            }
        }
        return best;
    }
}
