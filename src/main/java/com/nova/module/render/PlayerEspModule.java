package com.nova.module.render;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BoolSetting;
import com.nova.util.Projector;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class PlayerEspModule extends Module {
    private final BoolSetting names = bool("Namen + HP", true);
    private final BoolSetting distance = bool("Distanz", true);

    public PlayerEspModule() {
        super("Spieler ESP", "Umrandet alle Spieler, auch durch Wände.", Category.RENDER);
    }

    @Override
    public void onHud(DrawContext ctx, float tp) {
        if (!Projector.setup(mc, ctx, tp)) return;
        for (AbstractClientPlayerEntity pl : mc.world.getPlayers()) {
            if (pl == mc.player || !pl.isAlive()) continue;

            double dx = MathHelper.lerp(tp, pl.prevX, pl.getX()) - pl.getX();
            double dy = MathHelper.lerp(tp, pl.prevY, pl.getY()) - pl.getY();
            double dz = MathHelper.lerp(tp, pl.prevZ, pl.getZ()) - pl.getZ();
            Box box = pl.getBoundingBox().offset(dx, dy, dz);

            String label = null;
            if (names.get()) {
                label = pl.getName().getString() + " " + (int) Math.ceil(pl.getHealth()) + "HP";
                if (distance.get()) label += " [" + (int) mc.player.distanceTo(pl) + "m]";
            } else if (distance.get()) {
                label = (int) mc.player.distanceTo(pl) + "m";
            }
            Projector.drawEsp(ctx, mc.textRenderer, box, 0xFFFF4D4D, label);
        }
    }
}
