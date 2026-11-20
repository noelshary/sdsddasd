package com.nova.module;

import com.nova.module.combat.*;
import com.nova.module.movement.*;
import com.nova.module.render.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {}

    public static void init() {
        // Bewegung
        MODULES.add(new FlyModule());
        MODULES.add(new ElytraFlyModule());
        // Kampf
        MODULES.add(new StunSlamModule());
        MODULES.add(new ElytraStunSlamModule());
        MODULES.add(new AutoCrystalModule());
        MODULES.add(new AutoAnchorModule());
        MODULES.add(new AutoTotemModule());
        // Render
        MODULES.add(new PlayerEspModule());
        MODULES.add(new BlockEspModule());
        MODULES.add(new XRayModule());
        MODULES.add(new FullbrightModule());
    }

    public static List<Module> all() { return MODULES; }

    public static List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) if (m.category == c) out.add(m);
        return out;
    }

    public static <T extends Module> T get(Class<T> cls) {
        for (Module m : MODULES) if (cls.isInstance(m)) return cls.cast(m);
        return null;
    }

    public static void tick() {
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void renderHud(DrawContext ctx, RenderTickCounter counter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        float tp = counter.getTickProgress(false);
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try {
                m.onHud(ctx, tp);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // kleine Modulliste oben rechts
        int y = 4;
        int w = ctx.getScaledWindowWidth();
        ctx.drawTextWithShadow(mc.textRenderer, "Nova Client", 4, 4, 0xFF7C4DFF);
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            int tw = mc.textRenderer.getWidth(m.name);
            ctx.drawTextWithShadow(mc.textRenderer, m.name, w - tw - 4, y, 0xFFB39DFF);
            y += 10;
        }
    }
}
