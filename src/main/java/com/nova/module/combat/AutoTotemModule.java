package com.nova.module.combat;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.NumSetting;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemModule extends Module {
    private final NumSetting delay = num("Delay (Ticks)", 0, 0, 5, 1);
    private int wait;

    public AutoTotemModule() {
        super("Auto Totem", "Legt sofort nach einem Pop das nächste Totem in die Offhand.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
            wait = 0;
            return;
        }
        if (wait > 0) { wait--; return; }
        swap();
    }

    /** Wird direkt vom Netzwerk-Hook aufgerufen, sobald die Totem-Animation des Spielers ankommt. */
    public void onPop() {
        wait = 0;
        swap();
    }

    private void swap() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.currentScreenHandler != mc.player.playerScreenHandler) return;

        PlayerInventory inv = mc.player.getInventory();
        int found = -1;
        for (int i = 0; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                found = i;
                break;
            }
        }
        if (found < 0) return;

        int screenSlot = found < 9 ? 36 + found : found;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, screenSlot, 40, SlotActionType.SWAP, mc.player);
        wait = delay.getInt();
    }
}
