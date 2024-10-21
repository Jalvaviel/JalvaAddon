package com.jalvaviel.addon.BiomeESP.ESPBiomeData;

import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ESPBiomeDataScreen extends WindowScreen { // TODO doesn't show
    private final ESPBiomeData biomeData;
    private final Biome biome;
    private final BiomeDataSetting<ESPBiomeData> setting;

    public ESPBiomeDataScreen(GuiTheme theme, ESPBiomeData biomeData, Biome biome, BiomeDataSetting<ESPBiomeData> setting) {
        super(theme, "Configure Biome");

        this.biomeData = biomeData;
        this.biome = biome;
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        Settings settings = new Settings();
        SettingGroup sgGeneral = settings.getDefaultGroup();

        sgGeneral.add(new ColorSetting.Builder()
            .name("water-color")
            .description("Color of the water.")
            .defaultValue(new SettingColor(0, 50, 255,255))
            .onModuleActivated(settingColorSetting -> settingColorSetting.set(biomeData.waterColor))
            .onChanged(settingColor -> {
                biomeData.waterColor.set(settingColor);
                changed(biomeData, biome, setting);
            })
            .build()
        );

        sgGeneral.add(new ColorSetting.Builder()
            .name("sky-color")
            .description("Color of the sky.")
            .defaultValue(new SettingColor(100, 255, 255,255))
            .onModuleActivated(settingColorSetting -> settingColorSetting.set(biomeData.skyColor))
            .onChanged(settingColor -> {
                biomeData.skyColor.set(settingColor);
                changed(biomeData, biome, setting);
            })
            .build()
        );

        sgGeneral.add(new ColorSetting.Builder()
            .name("foliage-color")
            .description("Color of the foliage.")
            .defaultValue(new SettingColor(0, 255, 50,255))
            .onModuleActivated(settingColorSetting -> settingColorSetting.set(biomeData.foliageColor))
            .onChanged(settingColor -> {
                biomeData.foliageColor.set(settingColor);
                changed(biomeData, biome, setting);
            })
            .build()
        );

        sgGeneral.add(new ColorSetting.Builder()
            .name("grass-color")
            .description("Color of the grass.")
            .defaultValue(new SettingColor(0, 255, 50,255))
            .onModuleActivated(settingColorSetting -> settingColorSetting.set(biomeData.foliageColor))
            .onChanged(settingColor -> {
                biomeData.grassColor.set(settingColor);
                changed(biomeData, biome, setting);
            })
            .build()
        );

        settings.onActivated();
        add(theme.settings(settings)).expandX();
    }

    private void changed(ESPBiomeData biomeData, Biome biome, BiomeDataSetting<ESPBiomeData> setting) {
        if (!biomeData.isChanged() && biome != null && setting != null) {
            setting.get().put(biome, biomeData);
            setting.onChanged();
        }

        biomeData.changed();
    }
}
