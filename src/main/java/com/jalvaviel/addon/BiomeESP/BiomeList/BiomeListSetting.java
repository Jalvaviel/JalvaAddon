package com.jalvaviel.addon.BiomeESP.BiomeList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeListSetting extends Setting<Set<RegistryKey<Biome>>> {
    public final Predicate<RegistryKey<Biome>> filter;

    public BiomeListSetting(String name, String description, Set<RegistryKey<Biome>> defaultValue, Consumer<Set<RegistryKey<Biome>>> onChanged, Consumer<Setting<Set<RegistryKey<Biome>>>> onModuleActivated, IVisible visible, Predicate<RegistryKey<Biome>> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        value = new ObjectOpenHashSet<>(defaultValue);
    }

    @Override
    protected Set<RegistryKey<Biome>> parseImpl(String str) {
        String[] values = str.split(",");
        Set<RegistryKey<Biome>> biomes = new ObjectOpenHashSet<>(values.length);

        for (String value : values) {
            String name = value.trim();

            Identifier id;
            if (name.contains(":")) id = Identifier.of(name);
            else id = Identifier.ofVanilla(name);

            biomes.add(RegistryKey.of(RegistryKeys.BIOME, id));
        }

        return biomes;
    }

    @Override
    protected boolean isValueValid(Set<RegistryKey<Biome>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Optional.ofNullable(mc.getNetworkHandler())
            .flatMap(networkHandler -> networkHandler.getRegistryManager().getOptional(RegistryKeys.BIOME))
            .map(Registry::getIds).orElse(Set.of());
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (RegistryKey<Biome> biome : get()) {
            valueTag.add(NbtString.of(biome.getValue().toString()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    protected Set<RegistryKey<Biome>> load(NbtCompound tag) {
        get().clear();

        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            get().add(RegistryKey.of(RegistryKeys.BIOME, Identifier.of(tagI.asString())));
        }

        return get();
    }

    public static class Builder extends SettingBuilder<BiomeListSetting.Builder, Set<RegistryKey<Biome>>, BiomeListSetting> {
        private Predicate<RegistryKey<Biome>> filter;

        public Builder() {
            super(new ObjectOpenHashSet<>());
        }

        public BiomeListSetting.Builder defaultValue(RegistryKey<Biome>... defaults) {
            return defaultValue(defaults != null ? new ObjectOpenHashSet<>(defaults) : new ObjectOpenHashSet<>());
        }

        public BiomeListSetting.Builder filter(Predicate<RegistryKey<Biome>> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BiomeListSetting build() {
            return new BiomeListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
