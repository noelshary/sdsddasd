package com.nova.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public final class InvUtil {
    private InvUtil() {}

    public static int findHotbar(Predicate<ItemStack> test) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (test.test(mc.player.getInventory().getStack(i))) return i;
        }
        return -1;
    }

    public static int findHotbar(Item item) {
        return findHotbar(s -> s.isOf(item));
    }

    public static int selected() {
        return MinecraftClient.getInstance().player.getInventory().getSelectedSlot();
    }

    public static void select(int slot) {
        if (slot < 0 || slot > 8) return;
        MinecraftClient.getInstance().player.getInventory().setSelectedSlot(slot);
    }
}
