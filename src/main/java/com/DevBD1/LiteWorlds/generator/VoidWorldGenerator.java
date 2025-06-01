package com.DevBD1.LiteWorlds.generator;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {
    @Override
    public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
        return createChunkData(world); // empty chunk
    }
}