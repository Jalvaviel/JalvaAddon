package com.jalvaviel.addon.BiomeESP.BiomeList;


import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.DynamicRegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

import java.util.Set;

public class BiomeListSettingScreen extends DynamicRegistryListSettingScreen<Biome> {

    public BiomeListSettingScreen(GuiTheme theme, Setting<Set<RegistryKey<Biome>>> setting) {
        super(theme, "Select Biomes", setting, setting.get(), RegistryKeys.BIOME);
    }


    @Override
    protected WWidget getValueWidget(RegistryKey<Biome> value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(RegistryKey<Biome> value) {
        return value.getValue().toString();
    }
}

