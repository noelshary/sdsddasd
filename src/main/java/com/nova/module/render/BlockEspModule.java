package com.nova.module.render;

import com.nova.module.Category;
import com.nova.module.Module;
import com.nova.setting.BoolSetting;
import com.nova.setting.NumSetting;
import com.nova.util.Projector;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Block ESP: Umrandung durch Wände. Spawner zeigen den Mob-Namen darüber (Zombie grün, Skeleton grau, Spider schwarz ...).
 */
public class BlockEspModule extends Module {
    private final BoolSetting spawner = bool("Spawner", true);
    private final BoolSetting chests = bool("Truhen / Fässer", false);
    private final BoolSetting enderChests = bool("Endertruhen", false);
    private final BoolSetting shulkers = bool("Shulker", false);
    private final NumSetting chunkRadius = num("Chunk-Radius", 4, 1, 10, 1);

    private static final Pattern ID = Pattern.compile("id:\\s*[\"']?([a-z0-9_:.]+)[\"']?");

    private record Entry(BlockPos pos, String label, int color) {}

    private volatile List<Entry> entries = new ArrayList<>();

    public BlockEspModule() {
        super("Block ESP", "Spawner (mit Mob-Name), Truhen, Endertruhen, Shulker durch Wände sehen.", Category.RENDER);
    }

    @Override
    public void onDisable() {
        entries = new ArrayList<>();
    }

    @Override
    public void onTick() {
        if (mc.player.age % 10 != 0) return;
        List<Entry> found = new ArrayList<>();
        ChunkPos cp = mc.player.getChunkPos();
        int r = chunkRadius.getInt();

        for (int cx = -r; cx <= r; cx++) {
            for (int cz = -r; cz <= r; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cp.x + cx, cp.z + cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    Entry e = classify(be);
                    if (e != null) found.add(e);
                }
            }
        }
        entries = found;
    }

    private Entry classify(BlockEntity be) {
        BlockPos pos = be.getPos();
        if (be instanceof MobSpawnerBlockEntity && spawner.get()) {
            String key = spawnerMob(be);
            return new Entry(pos, "Spawner: " + pretty(key), mobColor(key));
        }
        if (be instanceof EnderChestBlockEntity && enderChests.get()) {
            return new Entry(pos, "Endertruhe", 0xFFAA00FF);
        }
        if (be instanceof ShulkerBoxBlockEntity && shulkers.get()) {
            return new Entry(pos, "Shulker", 0xFFFF77FF);
        }
        if ((be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) && chests.get()) {
            return new Entry(pos, be instanceof BarrelBlockEntity ? "Fass" : "Truhe", 0xFFD9A066);
        }
        return null;
    }

    /** Liest den Mob-Typ aus den (synchronisierten) Spawner-Daten. */
    private String spawnerMob(BlockEntity be) {
        try {
            String nbt = String.valueOf(be.createNbt(mc.world.getRegistryManager()));
            int idx = nbt.indexOf("SpawnData");
            if (idx >= 0) {
                Matcher m = ID.matcher(nbt.substring(idx));
                if (m.find()) {
                    String id = m.group(1);
                    return id.startsWith("minecraft:") ? id.substring(10) : id;
                }
            }
        } catch (Throwable ignored) {
        }
        return "unknown";
    }

    private static String pretty(String key) {
        if (key.equals("unknown")) return "Unbekannt";
        StringBuilder sb = new StringBuilder();
        for (String part : key.split("_")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private static int mobColor(String key) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "zombie" -> 0xFF2ECC40;
            case "skeleton" -> 0xFFAAAAAA;
            case "spider" -> 0xFF000000;
            case "cave_spider" -> 0xFF1B8A8A;
            case "blaze" -> 0xFFFFAA00;
            case "silverfish" -> 0xFF8FA3B8;
            case "magma_cube" -> 0xFFFF4D4D;
            default -> 0xFFFFFF55;
        };
    }

    @Override
    public void onHud(DrawContext ctx, float tp) {
        if (!Projector.setup(mc, ctx, tp)) return;
        for (Entry e : entries) {
            BlockPos p = e.pos();
            Box box = new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
            int dist = (int) Math.sqrt(mc.player.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5));
            Projector.drawEsp(ctx, mc.textRenderer, box, e.color(), e.label() + " [" + dist + "m]");
        }
    }
}
