package com.nova.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class TargetUtil {
    private TargetUtil() {}

    /** Nächster lebender Spieler (nicht man selbst) innerhalb der Reichweite, sonst null. */
    public static PlayerEntity nearestPlayer(double range) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return null;
        PlayerEntity best = null;
        double bestD = range * range;
        for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator() || p.getHealth() <= 0) continue;
            double d = mc.player.squaredDistanceTo(p);
            if (d <= bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }
}
