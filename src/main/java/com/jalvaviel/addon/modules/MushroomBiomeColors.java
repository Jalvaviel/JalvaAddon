package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
//import com.jalvaviel.addon.utils.ESPBiomeBlock;
//import com.jalvaviel.addon.utils.ESPBiomeChunk;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.util.math.BlockPos;

//import static com.jalvaviel.addon.utils.ESPBiomeChunk.searchChunk;


public class MushroomBiomeColors extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    //private final Long2ObjectMap<ESPBiomeChunk> chunks = new Long2ObjectOpenHashMap<>();
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private Dimension lastDimension;
    //private final List<ESPBiomeGroup> groups = new UnorderedArrayList<>();
    public final Setting<SettingColor> waterColor = sgGeneral.add(new ColorSetting.Builder()
        .name("water-color")
        .description("The color of the water.")
        .defaultValue(new SettingColor(200, 0, 200))
        .onChanged(val -> reload())
        .build()
    );
    public final Setting<SettingColor> deepOceanWaterColor = sgGeneral.add(new ColorSetting.Builder()
        .name("deep-water-color")
        .description("The color of the deep water.")
        .defaultValue(new SettingColor(0, 200, 200))
        .onChanged(val -> reload())
        .build()
    );
    public final Setting<SettingColor> skyColor = sgGeneral.add(new ColorSetting.Builder()
        .name("sky-color")
        .description("The color of the sky.")
        .defaultValue(new SettingColor(200, 0, 200))
        .onChanged(val -> reload())
        .build()
    );
    public final Setting<SettingColor> deepOceanSkyColor = sgGeneral.add(new ColorSetting.Builder()
        .name("deep-ocean-sky-color")
        .description("The color of the deep ocean sky.")
        .defaultValue(new SettingColor(0, 200, 200))
        .onChanged(val -> reload())
        .build()
    );
    public final Setting<Boolean> renderBlocks = sgGeneral.add(new BoolSetting.Builder()
        .name("renderBlocks")
        .description("Renders blocks within the biome such as air, water, etc.")
        .defaultValue(true)
        .build()
    );
    /*
    public final Setting<SettingColor> outlineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("outline-color")
        .description("The color of the outline.")
        .defaultValue(new SettingColor(200, 0, 200))
        .onChanged(val -> reload())
        .visible(() -> renderBlocks.get())
        .build()
    );

    public final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the side.")
        .defaultValue(new SettingColor(200, 0, 200, 20))
        .onChanged(val -> reload())
        .visible(() -> renderBlocks.get())
        .build()
    );
    public final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-y")
        .description("The minimum layer for the biome.")
        .sliderRange(-64,320)
        .defaultValue(-64)
        .build()
    );

    public final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
        .name("maximum-y")
        .description("The maximum layer for the biome.")
        .sliderRange(-64,320)
        .defaultValue(320)
        .build()
    );

    public final Setting<Boolean> occlusion = sgGeneral.add(new BoolSetting.Builder()
        .name("Occlusion")
        .description("Hide the faces that are covered by blocks")
        .defaultValue(true)
        .build()
    );

     */

    public MushroomBiomeColors() {
        super(Addon.CATEGORY, "Mushroom Water", "Changes the water color on Mushroom biomes.");
    }


   private void reload() {
        if (mc.worldRenderer != null && isActive()) mc.worldRenderer.reload();
    }

    /*
    public ESPBiomeBlock getBlock(int x, int y, int z) {
        ESPBiomeChunk chunk = chunks.get(ChunkPos.toLong(x >> 4, z >> 4));
        return chunk == null ? null : chunk.get(x, y, z);
    }

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

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        searchChunk(event.chunk());
    }

    private void updateChunk(int x, int z) {
        ESPBiomeChunk chunk = chunks.get(ChunkPos.toLong(x, z));
        if (chunk != null) chunk.update();
    }

    private void searchChunk(Chunk chunk) {
        MeteorExecutor.execute(() -> {
            if (!isActive()) return;
            ESPBiomeChunk schunk = ESPBiomeChunk.searchChunk(chunk);

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
    private void onPostTick(TickEvent.Post event) {
        Dimension dimension = PlayerUtils.getDimension();

        if (lastDimension != dimension) onActivate();

        lastDimension = dimension;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (renderBlocks.get()) {
            synchronized (chunks) {
                for (Iterator<ESPBiomeChunk> it = chunks.values().iterator(); it.hasNext(); ) {
                    ESPBiomeChunk chunk = it.next();

                    if (chunk.shouldBeDeleted()) {
                        MeteorExecutor.execute(() -> {
                            for (ESPBiomeBlock block : chunk.blocks.values()) {
                                block.loaded = false;
                            }
                        });

                        it.remove();
                    } else chunk.render(event);
                }
            }
        }
    }
*/
}

