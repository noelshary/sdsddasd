package com.nova.module.combat;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.NumSetting;
import com.nova.util.InvUtil;
import com.nova.util.TargetUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Fast Anchor: Anchor setzen -> mit Glowstone laden -> zünden.
 * Delay 0 = alle drei Schritte im selben Tick. Stoppt sofort, wenn das Ziel tot ist.
 * Funktioniert nur dort, wo Anchors explodieren (Overworld / End).
 */
public class AutoAnchorModule extends Module {
    private final NumSetting range = num("Ziel-Reichweite", 8, 3, 12, 0.5);
    private final NumSetting delay = num("Delay (Ticks)", 0, 0, 10, 1);
    private final NumSetting safeDist = num("Sicherheitsabstand", 3, 0, 6, 0.5);
    private final NumSetting minHp = num("Min. HP", 6, 0, 20, 1);

    private BlockPos anchorPos;
    private int wait;

    public AutoAnchorModule() {
        super("Fast Anchor", "Anchor setzen, laden und zünden. Stoppt, sobald das Ziel tot ist.", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        anchorPos = null;
        wait = 0;
    }

    @Override
    public void onTick() {
        if (wait > 0) { wait--; return; }
        ClientPlayerEntity p = mc.player;
        ClientWorld w = mc.world;

        PlayerEntity t = TargetUtil.nearestPlayer(range.get());
        if (t == null) { anchorPos = null; return; } // Ziel tot / weg -> aufhören
        if (p.getHealth() + p.getAbsorptionAmount() <= minHp.get()) return;

        int anchorSlot = InvUtil.findHotbar(Items.RESPAWN_ANCHOR);
        int glowSlot = InvUtil.findHotbar(Items.GLOWSTONE);

        if (anchorPos != null) {
            BlockState cur = w.getBlockState(anchorPos);
            boolean usable = cur.isOf(Blocks.RESPAWN_ANCHOR) || cur.isReplaceable();
            if (!usable || p.getEyePos().distanceTo(Vec3d.ofCenter(anchorPos)) > 4.6) anchorPos = null;
        }
        if (anchorPos == null) anchorPos = findPos(t, p, w);
        if (anchorPos == null) return;

        for (int step = 0; step < 3; step++) {
            if (!t.isAlive() || t.getHealth() <= 0) { anchorPos = null; return; }

            BlockState s = w.getBlockState(anchorPos);
            if (!s.isOf(Blocks.RESPAWN_ANCHOR)) {
                if (anchorSlot < 0) return;
                BlockHitResult hit = clickFor(anchorPos, w);
                if (hit == null) { anchorPos = null; return; }
                InvUtil.select(anchorSlot);
                mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
                p.swingHand(Hand.MAIN_HAND);
                if (!w.getBlockState(anchorPos).isOf(Blocks.RESPAWN_ANCHOR)) return;
            } else if (s.get(RespawnAnchorBlock.CHARGES) == 0) {
                if (glowSlot < 0) return;
                InvUtil.select(glowSlot);
                useAnchor(p);
            } else {
                int other = InvUtil.findHotbar(st -> !st.isOf(Items.GLOWSTONE));
                if (other >= 0) InvUtil.select(other);
                useAnchor(p);
                anchorPos = null;
                wait = delay.getInt();
                return;
            }
            if (delay.getInt() > 0) {
                wait = delay.getInt();
                return;
            }
        }
    }

    private void useAnchor(ClientPlayerEntity p) {
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(anchorPos), Direction.UP, anchorPos, false);
        mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, hit);
        p.swingHand(Hand.MAIN_HAND);
    }

    private BlockPos findPos(PlayerEntity t, ClientPlayerEntity p, ClientWorld w) {
        BlockPos tp = t.getBlockPos();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        double safe = safeDist.get() * safeDist.get();
        Vec3d eye = p.getEyePos();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = tp.add(dx, dy, dz);
                    if (!w.getBlockState(pos).isReplaceable()) continue;
                    Vec3d c = Vec3d.ofCenter(pos);
                    if (eye.distanceTo(c) > 4.5) continue;
                    if (p.squaredDistanceTo(c.x, c.y, c.z) < safe) continue;
                    if (clickFor(pos, w) == null) continue;
                    if (!w.getOtherEntities(null, new Box(pos)).isEmpty()) continue;

                    double d = c.squaredDistanceTo(t.getX(), t.getY() + 1.0, t.getZ());
                    if (d < bestD) {
                        bestD = d;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    /** Klick auf einen festen Nachbarblock, damit an {@code pos} etwas gesetzt werden kann. */
    private BlockHitResult clickFor(BlockPos pos, ClientWorld w) {
        for (Direction d : Direction.values()) {
            BlockPos n = pos.offset(d);
            if (w.getBlockState(n).isSolidBlock(w, n)) {
                Direction face = d.getOpposite();
                Vec3d hp = Vec3d.ofCenter(n).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
                return new BlockHitResult(hp, face, n, false);
            }
        }
        return null;
    }
}
