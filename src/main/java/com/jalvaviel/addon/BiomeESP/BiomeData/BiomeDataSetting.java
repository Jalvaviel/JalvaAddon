package com.jalvaviel.addon.BiomeESP.BiomeData;

import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import com.jalvaviel.addon.utils.VanillaBiomesRegKeys;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeDataSetting <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> extends Setting<Map<Biome, T>> {
    public final IGetter<T> defaultData;

    public BiomeDataSetting(String name, String description, Map<Biome, T> defaultValue, Consumer<Map<Biome, T>> onChanged, Consumer<Setting<Map<Biome, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.defaultData = defaultData;
    }

    @Override
    public void resetImpl() {
        value = new HashMap<>(defaultValue);
    }

    @Override
    protected Map<Biome, T> parseImpl(String str) {
        return new HashMap<>(0);
    }

    @Override
    protected boolean isValueValid(Map<Biome, T> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        //assert mc.world != null;
        NbtCompound valueTag = new NbtCompound();
        for (Biome biome : get().keySet()) {
            biome.getFoliageColor()
            valueTag.put(String.valueOf(VanillaBiomesRegKeys.getInstance().getBiomes().get(0).getRegistry()),
                get().get(biome).toTag());

            /*valueTag.put(Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome)).toString(),
                get().get(biome).toTag());
             */
        }
        tag.put("value", valueTag);
        return tag;
}
        /*
        if (mc.world == null) { // TODO debug this.
            for (Biome biome : get().keySet()) {
                Optional<RegistryEntry.Reference<Biome>> entry = BuiltinRegistries.createWrapperLookup().createRegistryLookup().getOptionalEntry(
                    RegistryKeys.BIOME, RegistryKey.of(RegistryKeys.BIOME,Identifier.of(biome.toString()))
                );
                biome = entry.orElseThrow().value(); // Reference implements RegistryEntry, this is fine
                valueTag.put(biome.toString(), get().get(biome).toTag());
            }
            tag.put("value", valueTag);
        } else {
            for (Biome biome : get().keySet()) {
                valueTag.put(Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome)).toString(),
                    get().get(biome).toTag());
            }
            tag.put("value", valueTag);
        }
        return tag;
    }

         */

    /*
    @Override
    protected Map<Biome, T> load(NbtCompound tag) {
        assert mc.world != null;
        get().clear();

        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            get().put(mc.world.getRegistryManager().get(RegistryKeys.BIOME).get(Identifier.of(key)), defaultData.get().copy().fromTag(valueTag.getCompound(key)));
        }

        return get();
    }
     */

    @Override
    protected Map<Biome, T> load(NbtCompound tag) {
        //assert mc.world != null;
        get().clear();
        Biome biome;
        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            get().put(mc.world.getRegistryManager().get(RegistryKeys.BIOME).get(Identifier.of(key)), defaultData.get().copy().fromTag(valueTag.getCompound(key)));
            if (mc.world == null) {
                Optional<RegistryEntry.Reference<Biome>> entry = BuiltinRegistries.createWrapperLookup().createRegistryLookup().getOptionalEntry(
                    RegistryKeys.BIOME, RegistryKey.of(RegistryKeys.BIOME,Identifier.of(key))
                );
                biome = entry.orElseThrow().value(); // Reference implements RegistryEntry, this is fine
                get().put(biome,defaultData.get().copy().fromTag(valueTag.getCompound(key)));
            } else {
                get().put(mc.world.getRegistryManager().get(RegistryKeys.BIOME).get(Identifier.of(key)), defaultData.get().copy().fromTag(valueTag.getCompound(key)));
            }
        }

        return get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> extends SettingBuilder<BiomeDataSetting.Builder<T>, Map<Biome, T>, BiomeDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(new HashMap<>(0));
        }

        public BiomeDataSetting.Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override
        public BiomeDataSetting<T> build() {
            return new BiomeDataSetting<>(name, description, defaultValue, onChanged, onModuleActivated, defaultData, visible);
        }
    }
}
