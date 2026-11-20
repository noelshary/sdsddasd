package com.nova.module.combat;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import com.nova.util.InvUtil;
import com.nova.util.TargetUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

/**
 * Mit der Elytra in einen Spieler reinfliegen: die Mod macht automatisch Axt/Schwert + Mace.
 * Du musst nichts switchen und nichts anklicken.
 */
public class ElytraStunSlamModule extends Module {
    private final NumSetting reach = num("Reichweite", 3.5, 2.0, 4.5, 0.1);
    private final NumSetting cooldown = num("Cooldown (Ticks)", 10, 1, 40, 1);
    private final BoolSetting swordCombo = bool("Schwert wenn kein Schild", true);
    private int cd;

    public ElytraStunSlamModule() {
        super("Elytra Stun Slam", "Mit Elytra reinfliegen, Mod macht Axt + Mace automatisch.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (cd > 0) { cd--; return; }
        ClientPlayerEntity p = mc.player;
        if (!p.isGliding()) return;

        int mace = InvUtil.findHotbar(Items.MACE);
        if (mace < 0) return;

        PlayerEntity t = TargetUtil.nearestPlayer(reach.get());
        if (t == null) return;

        int pre = StunSlamModule.pickPre(t, swordCombo.get());
        if (pre >= 0) StunSlamModule.hit(pre, t);
        StunSlamModule.hit(mace, t);
        cd = cooldown.getInt();
    }
}
