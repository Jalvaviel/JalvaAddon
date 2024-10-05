package com.jalvaviel.addon.BiomeESP.Biomes;

import com.jalvaviel.addon.BiomeESP.BiomeType;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BiomeListSetting extends Setting<List<BiomeType>> {
    public final Predicate<BiomeType> filter;

    public BiomeListSetting(String name, String description, List<BiomeType> defaultValue, Consumer<List<BiomeType>> onChanged, Consumer<Setting<List<BiomeType>>> onModuleActivated, IVisible visible, Predicate<BiomeType> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);

        this.filter = filter;
    }

    @Override
    protected List<BiomeType> parseImpl(String str) {
        String[] values = str.split(",");
        List<BiomeType> biomeTypes = new ArrayList<>(values.length);
        try {
            for (String value : values) {
                BiomeType biomeType = BiomeType.valueOf(value);
                if ((filter == null || filter.test(biomeType))) biomeTypes.add(biomeType);
            }
        } catch (Exception ignored) {}

        return biomeTypes;
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    @Override
    protected boolean isValueValid(List<BiomeType> value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return Arrays.stream(BiomeType.values())
            .map(Enum::name)
            .collect(Collectors.toList());
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (BiomeType biomeType : get()) {
            valueTag.add(NbtString.of(biomeType.toString()));
        }
        tag.put("value", valueTag);

        return tag;
    }

    @Override
    protected List<BiomeType> load(NbtCompound tag) {
        get().clear();

        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            BiomeType biomeType = BiomeType.valueOf(tagI.toString());
            if (filter == null || filter.test(biomeType)) get().add(biomeType);
        }

        return get();
    }

    public static class Builder extends SettingBuilder<BiomeListSetting.Builder, List<BiomeType>, BiomeListSetting> {
        private Predicate<BiomeType> filter;

        public Builder() {
            super(new ArrayList<>(0));
        }

        public BiomeListSetting.Builder defaultValue(BiomeType... defaults) {
            return defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList<>());
        }

        public BiomeListSetting.Builder filter(Predicate<BiomeType> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BiomeListSetting build() {
            return new BiomeListSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, filter);
        }
    }
}
