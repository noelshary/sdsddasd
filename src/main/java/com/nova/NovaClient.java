package com.nova;

import com.nova.gui.ClickGuiScreen;
import com.nova.module.ModuleManager;
import com.nova.module.combat.StunSlamModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class NovaClient implements ClientModInitializer {
    private static boolean shiftWasDown;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleMenuKey(client);
            if (client.player == null || client.world == null) return;
            ModuleManager.tick();
        });

        HudRenderCallback.EVENT.register(ModuleManager::renderHud);

        // Stun Slam hängt sich an jeden Angriff auf ein Lebewesen
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (player != mc.player) return ActionResult.PASS;
            StunSlamModule m = ModuleManager.get(StunSlamModule.class);
            if (m == null || !m.isEnabled()) return ActionResult.PASS;
            return m.onAttack(entity);
        });
    }

    /** Right Shift öffnet/schließt das Menü. */
    private static void handleMenuKey(MinecraftClient client) {
        long win = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !shiftWasDown) {
            if (client.currentScreen == null && client.player != null) {
                client.setScreen(new ClickGuiScreen());
            } else if (client.currentScreen instanceof ClickGuiScreen) {
                client.setScreen(null);
            }
        }
        shiftWasDown = down;
    }
}
