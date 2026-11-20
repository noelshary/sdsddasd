package com.nova.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

/**
 * Rechnet Weltkoordinaten auf den Bildschirm um (2D-ESP über das HUD, sieht durch Wände).
 * Funktioniert nur in der Ego-Perspektive.
 */
public final class Projector {
    private static double ex, ey, ez, fx, fy, fz, rx, ry, rz, ux, uy, uz, scale, cx, cy;

    private Projector() {}

    public static boolean setup(MinecraftClient mc, DrawContext ctx, float tp) {
        ClientPlayerEntity p = mc.player;
        if (p == null || !mc.options.getPerspective().isFirstPerson()) return false;

        ex = MathHelper.lerp(tp, p.prevX, p.getX());
        ey = MathHelper.lerp(tp, p.prevY, p.getY()) + (p.getEyeY() - p.getY());
        ez = MathHelper.lerp(tp, p.prevZ, p.getZ());

        double yaw = Math.toRadians(p.getYaw(tp));
        double pitch = Math.toRadians(p.getPitch(tp));

        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0;
        rz = -Math.sin(yaw);
        // up = right x forward
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        cx = w / 2.0;
        cy = h / 2.0;
        double fov = Math.toRadians(mc.options.getFov().getValue());
        scale = (h / 2.0) / Math.tan(fov / 2.0);
        return true;
    }

    private static double[] project(double x, double y, double z) {
        double dx = x - ex, dy = y - ey, dz = z - ez;
        double depth = dx * fx + dy * fy + dz * fz;
        if (depth < 0.1) return null;
        double right = dx * rx + dy * ry + dz * rz;
        double up = dx * ux + dy * uy + dz * uz;
        return new double[]{cx + right / depth * scale, cy - up / depth * scale};
    }

    /** Liefert {minX, minY, maxX, maxY} auf dem Bildschirm oder null, wenn die Box (teilweise) hinter dir liegt. */
    public static int[] box(Box b) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? b.minX : b.maxX;
            double y = (i & 2) == 0 ? b.minY : b.maxY;
            double z = (i & 4) == 0 ? b.minZ : b.maxZ;
            double[] s = project(x, y, z);
            if (s == null) return null;
            minX = Math.min(minX, s[0]);
            maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]);
            maxY = Math.max(maxY, s[1]);
        }
        if (maxX - minX > 8000 || maxY - minY > 8000) return null;
        return new int[]{(int) minX, (int) minY, (int) maxX, (int) maxY};
    }

    public static void outline(DrawContext c, int x1, int y1, int x2, int y2, int col) {
        c.fill(x1, y1, x2, y1 + 1, col);
        c.fill(x1, y2 - 1, x2, y2, col);
        c.fill(x1, y1, x1 + 1, y2, col);
        c.fill(x2 - 1, y1, x2, y2, col);
    }

    /** Zeichnet Umrandung + optionales Label über der Box. */
    public static void drawEsp(DrawContext ctx, TextRenderer tr, Box box, int color, String label) {
        int[] r = box(box);
        if (r == null) return;
        outline(ctx, r[0], r[1], r[2], r[3], color);
        if (label != null && !label.isEmpty()) {
            int tw = tr.getWidth(label);
            int x = (r[0] + r[2]) / 2 - tw / 2;
            int y = r[1] - 12;
            int plate = (color & 0xFFFFFF) == 0 ? 0xC0FFFFFF : 0x90000000;
            ctx.fill(x - 2, y - 1, x + tw + 2, y + 9, plate);
            ctx.drawTextWithShadow(tr, label, x, y, color);
        }
    }
}
