package com.nova.gui;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.module.ModuleManager;
import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import com.nova.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/**
 * Menü im Meteor-Stil: pro Kategorie eine Spalte.
 * Linksklick = Modul an/aus, Rechtsklick = Einstellungen auf-/zuklappen, Slider mit gedrückter Maus ziehen.
 * Die Maus wird per GLFW abgefragt (unabhängig von den Screen-Input-Methoden).
 */
public class ClickGuiScreen extends Screen {
    private static final int PW = 126, HH = 16, RH = 14, SH = 13;
    private static final int ACCENT = 0xFF7C4DFF;

    private final Set<Module> expanded = new HashSet<>();
    private boolean leftDown, rightDown;
    private NumSetting dragging;
    private int dragX;

    public ClickGuiScreen() {
        super(Text.literal("Nova Client"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long win = MinecraftClient.getInstance().getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean lClick = l && !leftDown;
        boolean rClick = r && !rightDown;
        leftDown = l;
        rightDown = r;
        if (!l) dragging = null;

        ctx.fill(0, 0, width, height, 0x80000000);

        String hint = null;
        int x = 16;
        for (Category cat : Category.values()) {
            int y = 16;
            ctx.fill(x, y, x + PW, y + HH, ACCENT);
            ctx.drawTextWithShadow(textRenderer, cat.label, x + 5, y + 4, 0xFFFFFFFF);
            y += HH;

            for (Module m : ModuleManager.byCategory(cat)) {
                boolean hover = inside(mx, my, x, y, PW, RH);
                int bg = m.isEnabled() ? 0xFF3B2A7A : (hover ? 0xFF2B2B2B : 0xFF1B1B1B);
                ctx.fill(x, y, x + PW, y + RH, bg);
                if (m.isEnabled()) ctx.fill(x, y, x + 2, y + RH, ACCENT);
                ctx.drawTextWithShadow(textRenderer, m.name, x + 6, y + 3, m.isEnabled() ? 0xFFFFFFFF : 0xFFBBBBBB);
                if (!m.settings.isEmpty()) {
                    ctx.drawTextWithShadow(textRenderer, expanded.contains(m) ? "-" : "+", x + PW - 10, y + 3, 0xFF888888);
                }
                if (hover) {
                    hint = m.description;
                    if (lClick) m.toggle();
                    if (rClick && !m.settings.isEmpty()) {
                        if (!expanded.remove(m)) expanded.add(m);
                    }
                }
                y += RH;

                if (expanded.contains(m)) {
                    for (Setting s : m.settings) {
                        y += drawSetting(ctx, s, x, y, mx, my, lClick);
                    }
                }
            }
            x += PW + 8;
        }

        if (dragging != null) {
            dragging.setRatio((mx - dragX) / (double) PW);
        }
        if (hint != null) {
            ctx.drawTextWithShadow(textRenderer, hint, 16, height - 14, 0xFFCCCCCC);
        }
    }

    private int drawSetting(DrawContext ctx, Setting s, int x, int y, int mx, int my, boolean lClick) {
        boolean hover = inside(mx, my, x, y, PW, SH);
        if (s instanceof BoolSetting b) {
            ctx.fill(x, y, x + PW, y + SH, hover ? 0xFF262626 : 0xFF161616);
            ctx.drawTextWithShadow(textRenderer, s.name, x + 8, y + 3, 0xFFCCCCCC);
            String v = b.get() ? "AN" : "AUS";
            ctx.drawTextWithShadow(textRenderer, v, x + PW - textRenderer.getWidth(v) - 5, y + 3, b.get() ? 0xFF7CFF8A : 0xFFFF7C7C);
            if (hover && lClick) b.toggle();
        } else if (s instanceof NumSetting n) {
            ctx.fill(x, y, x + PW, y + SH, 0xFF161616);
            ctx.fill(x, y, x + (int) (PW * n.ratio()), y + SH, 0xFF3B2A7A);
            String v = n.step >= 1 ? String.valueOf(n.getInt()) : String.valueOf(n.get());
            ctx.drawTextWithShadow(textRenderer, s.name, x + 8, y + 3, 0xFFCCCCCC);
            ctx.drawTextWithShadow(textRenderer, v, x + PW - textRenderer.getWidth(v) - 5, y + 3, 0xFFFFFFFF);
            if (hover && lClick) {
                dragging = n;
                dragX = x;
            }
        }
        return SH;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
