package com.jalvaviel.addon.BiomeESP.BiomeList;


import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.DynamicRegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.StringHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.stream.Collectors;

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

