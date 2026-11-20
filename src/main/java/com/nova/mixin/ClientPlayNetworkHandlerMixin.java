package com.nova.mixin;

import com.nova.module.ModuleManager;
import com.nova.module.combat.AutoTotemModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    /** Totem-Pop (Status 35) erkannt -> Auto Totem sofort, ohne auf den nächsten Tick zu warten. */
    @Inject(method = "onEntityStatus", at = @At("TAIL"), require = 0)
    private void nova$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() != 35) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        Entity e = packet.getEntity(mc.world);
        if (e != mc.player) return;
        AutoTotemModule totem = ModuleManager.get(AutoTotemModule.class);
        if (totem != null && totem.isEnabled()) totem.onPop();
    }
}
