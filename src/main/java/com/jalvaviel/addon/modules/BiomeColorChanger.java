package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.BiomeESP.BiomeType;
import com.jalvaviel.addon.BiomeESP.Biomes.BiomeListSetting;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.world.biome.BiomeKeys.DEEP_OCEAN;

//import static com.jalvaviel.addon.utils.ESPBiomeChunk.searchChunk;


public class BiomeColorChanger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Biome>> biomes = sgGeneral.add(new BiomeListSetting.Builder()
        .name("biomes")
        .description("Biomes to modify their colors.")
        .build()
    );

    public BiomeColorChanger() {
        super(Addon.CATEGORY, "biome-color-changer", "Change different biomes colors");
    }

    @Override
    public void onActivate() {
        assert mc.world != null;
        LogUtils.getLogger().info(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getIds().toString());
    }
    //private final List<ESPBiomeGroup> groups = new UnorderedArrayList<>();
    /*
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
*/
}

