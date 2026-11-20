package com.nova.module.movement;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import net.minecraft.client.network.ClientPlayerEntity;

public class FlyModule extends Module {
    private final NumSetting speed = num("Speed", 1.0, 0.1, 5.0, 0.1);
    private final BoolSetting noClip = bool("Durch Blöcke fliegen", false);

    public FlyModule() {
        super("Fly", "Frei fliegen (Space hoch, Shift runter). Optional durch Blöcke.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (p.isGliding()) return;

        double yaw = Math.toRadians(p.getYaw());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw);

        double mx = 0, mz = 0;
        if (mc.options.forwardKey.isPressed()) { mx += fx; mz += fz; }
        if (mc.options.backKey.isPressed())    { mx -= fx; mz -= fz; }
        if (mc.options.rightKey.isPressed())   { mx += rx; mz += rz; }
        if (mc.options.leftKey.isPressed())    { mx -= rx; mz -= rz; }

        double len = Math.sqrt(mx * mx + mz * mz);
        if (len > 0) { mx /= len; mz /= len; }

        double vy = 0;
        if (mc.options.jumpKey.isPressed()) vy += speed.get();
        if (mc.options.sneakKey.isPressed()) vy -= speed.get();

        p.setVelocity(mx * speed.get(), vy, mz * speed.get());
        p.noClip = noClip.get();
        if (noClip.get()) p.setOnGround(false);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.noClip = false;
    }
}
