package com.nova.module.movement;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.NumSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ElytraFlyModule extends Module {
    private final NumSetting speed = num("Speed", 1.5, 0.2, 5.0, 0.1);

    public ElytraFlyModule() {
        super("Elytra Fly", "Kontrolliertes Elytra-Fliegen: W/S = vor/zurück, Space/Shift = hoch/runter, sonst Stillstand.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity p = mc.player;
        if (!p.isGliding()) return;

        Vec3d look = p.getRotationVec(1.0f);
        double s = speed.get();
        if (mc.options.forwardKey.isPressed()) {
            p.setVelocity(look.x * s, look.y * s, look.z * s);
        } else if (mc.options.backKey.isPressed()) {
            p.setVelocity(-look.x * s, -look.y * s, -look.z * s);
        } else if (mc.options.jumpKey.isPressed()) {
            p.setVelocity(0, s, 0);
        } else if (mc.options.sneakKey.isPressed()) {
            p.setVelocity(0, -s, 0);
        } else {
            p.setVelocity(0, 0, 0);
        }
    }
}
