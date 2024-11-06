package com.jalvaviel.addon.BiomeESP.BiomeData;

import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import com.jalvaviel.addon.modules.BiomeColorChanger;
import com.jalvaviel.addon.utils.VanillaBiomesRegKeys;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.jalvaviel.addon.modules.BiomeColorChanger.FALLBACK_KEYS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeDataSetting <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> extends Setting<Map<RegistryKey<Biome>, T>> {
    public final IGetter<T> defaultData;

    public BiomeDataSetting(String name, String description, Map<RegistryKey<Biome>, T> defaultValue, Consumer<Map<RegistryKey<Biome>, T>> onChanged, Consumer<Setting<Map<RegistryKey<Biome>, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        if (defaultValue.isEmpty()) {
            for (RegistryKey<Biome> biome : FALLBACK_KEYS) {
                defaultValue.put(biome, defaultData.get().copy());
            }
        }
        this.defaultData = defaultData;
    }

    @Override
    public void onChanged() {
        if(mc.world != null && Modules.get().get(BiomeColorChanger.class).isActive()){
            mc.worldRenderer.reload(); //gameRenderer.getBlockRenderer().clearStateTextures();
        }
    }

    @Override
    public void resetImpl() {
        value = new HashMap<>(defaultValue);
    }

    @Override
    protected Map<RegistryKey<Biome>, T> parseImpl(String str) { // TODO Listen to this.
        return new HashMap<>(63);
    }

    @Override
    protected boolean isValueValid(Map<RegistryKey<Biome>, T> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        //assert mc.world != null;
        NbtCompound valueTag = new NbtCompound();
        for (RegistryKey<Biome> biome : get().keySet()) {
            valueTag.put(biome.getValue().toString(), get().get(biome).toTag()); // TODO CHECK
        }
        tag.put("value", valueTag);
        return tag;
    }

    @Override
    protected Map<RegistryKey<Biome>, T> load(NbtCompound tag) {
        get().clear();
        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            get().put(RegistryKey.of(RegistryKeys.BIOME, Identifier.of(key)), defaultData.get().copy().fromTag(valueTag.getCompound(key)));
        }

        return get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> extends SettingBuilder<Builder<T>, Map<RegistryKey<Biome>, T>, BiomeDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(new HashMap<>(63));
        }

        public Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override
        public BiomeDataSetting<T> build() {
            return new BiomeDataSetting<>(name, description, defaultValue, onChanged, onModuleActivated, defaultData, visible);
        }
    }
}
