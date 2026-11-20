package com.nova.mixin;

import com.nova.module.ModuleManager;
import com.nova.module.render.XRayModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true, require = 0)
    private static void nova$xray(BlockState state, BlockState otherState, Direction side, CallbackInfoReturnable<Boolean> cir) {
        XRayModule xray = ModuleManager.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(xray.isVisible(state.getBlock()));
        }
    }
}
