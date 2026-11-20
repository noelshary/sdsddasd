package com.nova.module;

import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import com.nova.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected BoolSetting bool(String name, boolean def) {
        BoolSetting s = new BoolSetting(name, def);
        settings.add(s);
        return s;
    }

    protected NumSetting num(String name, double def, double min, double max, double step) {
        NumSetting s = new NumSetting(name, def, min, max, step);
        settings.add(s);
        return s;
    }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onHud(DrawContext ctx, float tickProgress) {}
}
