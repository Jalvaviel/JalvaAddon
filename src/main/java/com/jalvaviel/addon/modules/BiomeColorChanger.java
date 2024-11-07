package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeData;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.stream.Collectors;

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
        .onChanged(value -> reload())
        .build()
    );
    public final Setting<ESPBiomeData> defaultBiomeConfig = sgGeneral.add(new GenericSetting.Builder<ESPBiomeData>() // TODO Change to true biome defaults
        .name("default-biome-config")
        .description("Default biome config.")
        .defaultValue(
            new ESPBiomeData(
                new SettingColor(0, 50, 255),
                new SettingColor(100, 255, 255),
                new SettingColor(0, 255, 50),
                new SettingColor(0, 255, 100)
            )
        )
        .onChanged(value -> reload())
        .build()
    );

    public final Setting<Map<RegistryKey<Biome>, ESPBiomeData>> biomeConfigs = sgGeneral.add(new BiomeDataSetting.Builder<ESPBiomeData>()
        .name("biome-configs")
        .description("Config for each biome.")
        .defaultData(defaultBiomeConfig)
        .onChanged(value -> reload())
        .build()
    );

    public BiomeColorChanger() {
        super(Addon.CATEGORY, "biome-color-changer", "Change different biomes colors");
    }

    private void reload() {
        if (mc.worldRenderer != null && isActive()) mc.worldRenderer.reload();
    }

    @Override
    public void onActivate() {
        reload();
    }
    @Override
    public void onDeactivate() {
        reload();
    }
}

