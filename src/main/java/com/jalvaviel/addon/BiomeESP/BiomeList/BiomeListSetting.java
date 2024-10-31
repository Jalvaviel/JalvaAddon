package com.jalvaviel.addon.BiomeESP.BiomeList;
import com.jalvaviel.addon.modules.BiomeColorChanger;
import com.jalvaviel.addon.utils.VanillaBiomesRegKeys;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeListSetting extends Setting<List<String>> {
    public final Predicate<String> filter;

    public BiomeListSetting(String name, String description, List<String> defaultValue, Consumer<List<String>> onChanged, Consumer<Setting<List<String>>> onModuleActivated, IVisible visible, Predicate<String> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    @Override
    protected List<String> parseImpl(String str) {
        assert mc.world != null;
        String biome;
        String[] values = str.split(",");
        List<String> biomes = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                if (value != null && (filter == null || filter.test(value))) biomes.add(value);
            }
        } catch (Exception ignored) {}

        return biomes;
    }

    @Override
    protected boolean isValueValid(List<String> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return VanillaBiomesRegKeys.getInstance().getBiomeIds();
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        //assert mc.world != null;
        NbtList valueTag = new NbtList();
        for (String biome : get()) {
            valueTag.add(NbtString.of(biome));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    protected List<String> load(NbtCompound tag) {
        //assert mc.world != null;
        String biome;
        get().clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            biome = tagI.asString();
            if (filter == null || filter.test(biome)) get().add(biome);
        }
        return get();
    }

    public void onChanged() {
        if(mc.world != null && Modules.get().get(BiomeColorChanger.class).isActive()){
            mc.worldRenderer.reload(); //gameRenderer.getBlockRenderer().clearStateTextures();
        }
    }

    public static class Builder extends SettingBuilder<Builder, List<String>, BiomeListSetting> {
        private Predicate<String> filter;

        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(String... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        public Builder filter(Predicate<String> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BiomeListSetting build() {
            return new BiomeListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
