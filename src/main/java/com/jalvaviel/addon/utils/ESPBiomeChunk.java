package com.jalvaviel.addon.utils;

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

import com.jalvaviel.addon.modules.MushroomBiomeColors;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import java.util.List;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.Utils.getRenderDistance;

/*
public class ESPBiomeChunk {
    private final int x, z;
    public Long2ObjectMap<ESPBiomeBlock> blocks;
    public static final MushroomBiomeColors mushroomBiomeColors = Modules.get().get(MushroomBiomeColors.class);
    public ESPBiomeChunk(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public ESPBiomeBlock get(int x, int y, int z) {
        return blocks == null ? null : blocks.get(ESPBiomeBlock.getKey(x, y, z));
    }

    public void add(BlockPos blockPos, boolean update) {
        ESPBiomeBlock block = new ESPBiomeBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if (blocks == null) blocks = new Long2ObjectOpenHashMap<>(64);
        blocks.put(ESPBiomeBlock.getKey(blockPos), block);
        if (update) block.update();
    }

    public void add(BlockPos blockPos) {
        add(blockPos, false); //true
    }


    public int size() {
        return blocks == null ? 0 : blocks.size();
    }

    public boolean shouldBeDeleted() {
        int viewDist = getRenderDistance() + 1;
        int chunkX = ChunkSectionPos.getSectionCoord(mc.player.getBlockPos().getX());
        int chunkZ = ChunkSectionPos.getSectionCoord(mc.player.getBlockPos().getZ());

        return x > chunkX + viewDist || x < chunkX - viewDist || z > chunkZ + viewDist || z < chunkZ - viewDist;
    }
    public void update() {

        if (blocks != null) {
            for (ESPBiomeBlock block : blocks.values()) block.update();
        }
    }

    public void render(Render3DEvent event) {
        event.renderer.triangles.depthTest = mushroomBiomeColors.occlusion.get();
        event.renderer.lines.depthTest = mushroomBiomeColors.occlusion.get();
        if (blocks != null) {
            for (ESPBiomeBlock block : blocks.values()) {
                block.render(event);
                //LOG.debug(block.toString());
            }
        }
    }
    public static boolean differentBiomeNeighbours(BlockPos blockPos) {
        // Check direct neighbors
        if (isNeighbourDifferentBiome(blockPos, Direction.SOUTH)) return true;
        if (isNeighbourDifferentBiome(blockPos, Direction.EAST)) return true;
        if (isNeighbourDifferentBiome(blockPos, Direction.NORTH)) return true;
        if (isNeighbourDifferentBiome(blockPos, Direction.WEST)) return true;
        if (isNeighbourDifferentBiome(blockPos, Direction.UP)) return true;
        if (isNeighbourDifferentBiome(blockPos, Direction.DOWN)) return true;

        // Check diagonal neighbors
        if (isNeighbourDifferentBiomeDiagonal(blockPos, 1, 0, 1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, 1, 0, -1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, -1, 0, -1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, -1, 0, 1)) return true;

        if (isNeighbourDifferentBiomeDiagonal(blockPos, 0, 1, 1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, 0, 1, -1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, 1, 1, 0)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, -1, 1, 0)) return true;

        if (isNeighbourDifferentBiomeDiagonal(blockPos, 0, -1, 1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, 0, -1, -1)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, 1, -1, 0)) return true;
        if (isNeighbourDifferentBiomeDiagonal(blockPos, -1, -1, 0)) return true;

        // If no neighboring blocks are in a different biome, return false
        return false;
    }


    private static boolean isNeighbourDifferentBiome(BlockPos blockPos, Direction dir) {
        RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();
        BlockPos neighbour = new BlockPos(blockPos.getX() + dir.getOffsetX(), blockPos.getY() + dir.getOffsetY(), blockPos.getZ() + dir.getOffsetZ());
        RegistryKey<Biome> biomeNeighbour = mc.world.getBiome(neighbour).getKey().get();

        return !biomeNeighbour.equals(biome);
    }

    private static boolean isNeighbourDifferentBiomeDiagonal(BlockPos blockPos, int x, int y, int z) {
        RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();
        BlockPos neighbour = new BlockPos(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);
        RegistryKey<Biome> biomeNeighbour = mc.world.getBiome(neighbour).getKey().get();

        return !biomeNeighbour.equals(biome);
    }


    public static ESPBiomeChunk searchChunk(Chunk chunk) {

        ESPBiomeChunk schunk = new ESPBiomeChunk(chunk.getPos().x, chunk.getPos().z);
        if (schunk.shouldBeDeleted()) return schunk;

        BlockPos.Mutable blockPos = new BlockPos.Mutable();

        for (int x = chunk.getPos().getStartX(); x <= chunk.getPos().getEndX(); x++) {
            for (int z = chunk.getPos().getStartZ(); z <= chunk.getPos().getEndZ(); z++) {
                //int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x - chunk.getPos().getStartX(), z - chunk.getPos().getStartZ());
                for (int y = mushroomBiomeColors.minY.get(); y < mushroomBiomeColors.maxY.get(); y++) {
                    blockPos.set(x, y, z);
                    //BlockState bs = chunk.getBlockState(blockPos);
                    RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();
                    //if (bs.getBlock() == Blocks.AIR && biome == BiomeKeys.MUSHROOM_FIELDS) schunk.add(blockPos, false);
                    if ((biome == BiomeKeys.MUSHROOM_FIELDS) && differentBiomeNeighbours(blockPos)) schunk.add(blockPos, false); //bs.getBlock() == Blocks.AIR &&
                }
            }
        }

        return schunk;
    }
}
 */

