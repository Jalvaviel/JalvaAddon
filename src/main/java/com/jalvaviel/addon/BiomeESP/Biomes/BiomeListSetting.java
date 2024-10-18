package com.jalvaviel.addon.BiomeESP.Biomes;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeListSetting extends Setting<List<Biome>> {
    public final Predicate<Biome> filter;

    public BiomeListSetting(String name, String description, List<Biome> defaultValue, Consumer<List<Biome>> onChanged, Consumer<Setting<List<Biome>>> onModuleActivated, IVisible visible, Predicate<Biome> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    @Override
    protected List<Biome> parseImpl(String str) {
        assert mc.world != null;
        String[] values = str.split(",");
        List<Biome> biomes = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                Biome biome = parseId(mc.world.getRegistryManager().get(RegistryKeys.BIOME), value);
                if (biome != null && (filter == null || filter.test(biome))) biomes.add(biome);
            }
        } catch (Exception ignored) {}

        return biomes;
    }

    @Override
    protected boolean isValueValid(List<Biome> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        assert mc.world != null;
        return mc.world.getRegistryManager().get(RegistryKeys.BIOME).getIds();
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        assert mc.world != null;
        NbtList valueTag = new NbtList();
        for (Biome biome : get()) {
            valueTag.add(NbtString.of(Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome)).toString()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    protected List<Biome> load(NbtCompound tag) {
        assert mc.world != null;
        get().clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            Biome biome = mc.world.getRegistryManager().get(RegistryKeys.BIOME).get(Identifier.of(tagI.asString()));
            if (filter == null || filter.test(biome)) get().add(biome);
        }
        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<Biome>, BiomeListSetting> {
        private Predicate<Biome> filter;

        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(Biome... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        public Builder filter(Predicate<Biome> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BiomeListSetting build() {
            return new BiomeListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
