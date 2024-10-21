package com.jalvaviel.addon.BiomeESP.BiomeList;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeListSettingScreen extends RegistryListSettingScreen<Biome> {

    public BiomeListSettingScreen(GuiTheme theme, Setting<List<Biome>> setting) {
        super(theme, "Select Biomes", setting, setting.get(), Objects.requireNonNull(mc.world).getRegistryManager().get(RegistryKeys.BIOME));
    }

    @Override
    protected boolean includeValue(Biome value) {
        Predicate<Biome> filter = ((BiomeListSetting) setting).filter;

        if (filter == null) return true;
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(Biome value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(Biome value) {
        assert mc.world != null;
        return Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getEntry(value).getIdAsString());
        /*
        if (mc.world == null) {
            Optional<RegistryEntry.Reference<Biome>> entry = BuiltinRegistries.createWrapperLookup().createRegistryLookup().getOptionalEntry(
                RegistryKeys.BIOME, RegistryEntry.of(value).getKey().get());
            return entry.orElseThrow().value().toString(); // Reference implements RegistryEntry, this is fine
        } else {
            return Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(value)).toString();
        }
         */

    }
}
