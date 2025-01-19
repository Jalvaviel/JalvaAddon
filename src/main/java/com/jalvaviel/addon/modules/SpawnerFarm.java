package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPChunk;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpawnerFarm extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select specific entities.")
        .defaultValue(EntityType.SKELETON)
        .build()
    );

    private final Setting<SettingColor> outlineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("outline-color")
        .description("The color of the outline")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the side")
        .defaultValue(new SettingColor(255, 0, 0, 64))
        .build()
    );
    private final Setting<Boolean> occlusion = sgGeneral.add(new BoolSetting.Builder()
        .name("occlusion-culling")
        .description("Hide the faces that are covered by blocks")
        .defaultValue(false)
        .build()
    );

    public SpawnerFarm() {
        super(Addon.CATEGORY, "spawner-farm", "Overlays the spawners with certain conditions");
    }

    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();

    private final Long2ObjectMap<ESPChunk> chunks = new Long2ObjectOpenHashMap<>();
    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();

    private Dimension lastDimension;

    @Override
    public void onActivate() {
        synchronized (chunks) {
            chunks.clear();
        }

        for (Chunk chunk : Utils.chunks()) {
            searchChunk(chunk);
        }

        lastDimension = PlayerUtils.getDimension();
    }

    @Override
    public void onDeactivate() {
        synchronized (chunks) {
            chunks.clear();
        }
    }

    private void updateChunk(int x, int z) {
        ESPChunk chunk = chunks.get(ChunkPos.toLong(x, z));
        if (chunk != null) chunk.update();
    }

    private void updateBlock(int x, int y, int z) {
        ESPChunk chunk = chunks.get(ChunkPos.toLong(x >> 4, z >> 4));
        if (chunk != null) chunk.update(x, y, z);
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        searchChunk(event.chunk());
    }

    private void searchChunk(Chunk chunk) {
        workerThread.submit(() -> {
            if (!isActive()) return;
            List<Block> spawner = List.of(Blocks.SPAWNER);
            ESPChunk schunk = ESPChunk.searchChunk(chunk, spawner);

            if (schunk.size() > 0) {
                synchronized (chunks) {
                    chunks.put(chunk.getPos().toLong(), schunk);
                    schunk.update();

                    // Update neighbour chunks
                    updateChunk(chunk.getPos().x - 1, chunk.getPos().z);
                    updateChunk(chunk.getPos().x + 1, chunk.getPos().z);
                    updateChunk(chunk.getPos().x, chunk.getPos().z - 1);
                    updateChunk(chunk.getPos().x, chunk.getPos().z + 1);
                }
            }
        });
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        // Minecraft probably reuses the event.pos BlockPos instance because it causes problems when trying to use it inside another thread
        int bx = event.pos.getX();
        int by = event.pos.getY();
        int bz = event.pos.getZ();

        int chunkX = bx >> 4;
        int chunkZ = bz >> 4;
        long key = ChunkPos.toLong(chunkX, chunkZ);

            workerThread.submit(() -> {
                synchronized (chunks) {
                    ESPChunk chunk = chunks.get(key);

                    if (chunk == null) {
                        chunk = new ESPChunk(chunkX, chunkZ);
                        if (chunk.shouldBeDeleted()) return;

                        chunks.put(key, chunk);
                    }

                    blockPos.set(bx, by, bz);

                    if (event.newState.getBlock() == Blocks.SPAWNER) chunk.add(blockPos);
                    else chunk.remove(blockPos);

                    // Update neighbour blocks
                    for (int x = -1; x < 2; x++) {
                        for (int z = -1; z < 2; z++) {
                            for (int y = -1; y < 2; y++) {
                                if (x == 0 && y == 0 && z == 0) continue;

                                updateBlock(bx + x, by + y, bz + z);
                            }
                        }
                    }
                }
            });
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        Dimension dimension = PlayerUtils.getDimension();
        if (lastDimension != dimension) onActivate();
        lastDimension = dimension;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        synchronized (chunks) {
            for (Iterator<ESPChunk> it = chunks.values().iterator(); it.hasNext(); ) {
                ESPChunk chunk = it.next();

                if (chunk.shouldBeDeleted()) {
                    workerThread.submit(() -> {
                        for (ESPBlock block : chunk.blocks.values()) {
                            block.group.remove(block, false);
                            block.loaded = false;
                        }
                    });

                    it.remove();
                } else if (chunk.blocks != null) {
                    // Spatial hash map: Map cell coordinates to a list of blocks
                    Map<String, List<ESPBlock>> spatialHash = new HashMap<>();
                    int cellSize = 32; // Size of each spatial hash cell

                    // Populate the spatial hash map
                    for (ESPBlock block : chunk.blocks.values()) {
                        String cellKey = getCellKey(block.x, block.y, block.z, cellSize);
                        spatialHash.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(block);
                    }

                    // Render blocks
                    for (ESPBlock block : chunk.blocks.values()) {
                        int neighbors = 0;

                        // Check neighbors only in the surrounding cells
                        String[] nearbyCells = getNearbyCellKeys(block.x, block.y, block.z, cellSize);
                        for (String cellKey : nearbyCells) {
                            List<ESPBlock> nearbyBlocks = spatialHash.getOrDefault(cellKey, Collections.emptyList());
                            for (ESPBlock neighbor : nearbyBlocks) {
                                if (!block.equals(neighbor)) {
                                    double distanceSquared =
                                        Math.pow(block.x - neighbor.x, 2) +
                                            Math.pow(block.y - neighbor.y, 2) +
                                            Math.pow(block.z - neighbor.z, 2);

                                    if (distanceSquared <= 31 * 31) {
                                        neighbors++;
                                    }
                                }
                            }
                        }

                        // Normalize neighbors for HSV (e.g., scale between 0 and 360)
                        float hue = Math.min(360, neighbors * 40);
                        Color blockColor = new Color((java.awt.Color.HSBtoRGB(hue,255,255) & 0xFFFFFF00) | (128 & 0xFF));

                        // Render block with lines and sides
                        event.renderer.blockLines(block.x, block.y, block.z, blockColor, 0);
                        event.renderer.blockSides(block.x, block.y, block.z, blockColor, 0);
                    }
                }
            }
        }
    }

    // Helper to compute a cell key for spatial hashing
    private String getCellKey(double x, double y, double z, int cellSize) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        int cellZ = (int) Math.floor(z / cellSize);
        return cellX + "," + cellY + "," + cellZ;
    }

    // Helper to get surrounding cell keys
    private String[] getNearbyCellKeys(double x, double y, double z, int cellSize) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        int cellZ = (int) Math.floor(z / cellSize);

        List<String> nearbyKeys = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    nearbyKeys.add((cellX + dx) + "," + (cellY + dy) + "," + (cellZ + dz));
                }
            }
        }
        return nearbyKeys.toArray(new String[0]);
    }
}
