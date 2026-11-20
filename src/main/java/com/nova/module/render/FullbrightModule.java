package com.nova.module.render;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullbrightModule extends Module {
    public FullbrightModule() {
        super("Fullbright", "Überall hell (Nachtsicht nur auf deinem Client).", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)
                || mc.player.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration() < 300) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
