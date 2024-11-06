package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
//import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
//import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;
//import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeData;

import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;

import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeData;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.stream.Collectors;

//import static com.jalvaviel.addon.utils.ESPBiomeChunk.searchChunk;


public class BiomeColorChanger extends Module {
    public static final Set<RegistryKey<Biome>> FALLBACK_KEYS;

    static {
        //noinspection unchecked
        FALLBACK_KEYS = (Set<RegistryKey<Biome>>) (Object) Arrays.stream(BiomeKeys.class.getDeclaredFields())
            .filter(field -> field.getType() == RegistryKey.class)
            .filter(field -> field.accessFlags().containsAll(List.of(AccessFlag.STATIC, AccessFlag.PUBLIC)))
            .map(field -> {
                try {
                    return field.get(null);
                } catch (Throwable t) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Set<RegistryKey<Biome>>> biomes = sgGeneral.add(new BiomeListSetting.Builder()
        .name("biomes")
        .description("Biomes to modify their colors.")
        .build()
    );
    public final Setting<ESPBiomeData> defaultBiomeConfig = sgGeneral.add(new GenericSetting.Builder<ESPBiomeData>() // TODO Change to true biome defaults
        .name("default-biome-config")
        .description("Default biome config.")
        .defaultValue(
            new ESPBiomeData(
                new SettingColor(0, 50, 255, 255),
                new SettingColor(100, 255, 255, 255),
                new SettingColor(0, 255, 50, 255),
                new SettingColor(0, 255, 100, 255)
            )
        )
        .build()
    );

    public final Setting<Map<RegistryKey<Biome>, ESPBiomeData>> biomeConfigs = sgGeneral.add(new BiomeDataSetting.Builder<ESPBiomeData>()
        .name("biome-configs")
        .description("Config for each biome.")
        .defaultData(defaultBiomeConfig)
        .build()
    );
    public BiomeColorChanger() {
        super(Addon.CATEGORY, "biome-color-changer", "Change different biomes colors");
    }
}
/*


    public BiomeColorChanger() {
        super(Addon.CATEGORY, "biome-color-changer", "Change different biomes colors");
    }

    @Override
    public void onActivate() {
        if(mc.world != null){
            mc.worldRenderer.reload(); //gameRenderer.getBlockRenderer().clearStateTextures();
        }
    }
    @Override
    public void onDeactivate() {
        if(mc.world != null){
            mc.worldRenderer.reload(); //gameRenderer.getBlockRenderer().clearStateTextures();
        }
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


