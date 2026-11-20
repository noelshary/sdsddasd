package com.nova.module.render;

import com.nova.module.Category;
import com.nova.module.Module;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;

/** X-Ray: nur Erze und wichtige Blöcke werden gezeichnet (Block.shouldDrawSide wird per Mixin umgebogen). */
public class XRayModule extends Module {
    private final Map<Block, Boolean> cache = new HashMap<>();

    public XRayModule() {
        super("X-Ray", "Nur Erze, Spawner, Truhen usw. sichtbar. Der Rest wird unsichtbar.", Category.RENDER);
    }

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        reload();
    }

    private void reload() {
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }

    public boolean isVisible(Block block) {
        return cache.computeIfAbsent(block, b -> {
            String id = Registries.BLOCK.getId(b).getPath();
            return id.endsWith("_ore")
                    || id.equals("ancient_debris")
                    || id.equals("nether_quartz_ore")
                    || id.equals("spawner")
                    || id.equals("trial_spawner")
                    || id.equals("vault")
                    || id.equals("chest")
                    || id.equals("trapped_chest")
                    || id.equals("ender_chest")
                    || id.equals("barrel")
                    || id.endsWith("shulker_box")
                    || id.equals("beacon")
                    || id.equals("respawn_anchor")
                    || id.equals("diamond_block")
                    || id.equals("emerald_block")
                    || id.equals("gold_block")
                    || id.equals("netherite_block")
                    || id.equals("iron_block")
                    || id.equals("lapis_block")
                    || id.equals("redstone_block");
        });
    }
}
