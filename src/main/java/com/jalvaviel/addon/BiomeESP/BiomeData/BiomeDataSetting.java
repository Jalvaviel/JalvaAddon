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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeDataSetting <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> extends Setting<Map<String, T>> {
    public final IGetter<T> defaultData;

    public BiomeDataSetting(String name, String description, Map<String, T> defaultValue, Consumer<Map<String, T>> onChanged, Consumer<Setting<Map<String, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        if (defaultValue.isEmpty()) { // TODO CHECK
            for (String biome : VanillaBiomesRegKeys.getInstance().getBiomes()) {
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
        value = new HashMap<>(defaultValue); // TODO Value is a HashMap with size 0 until the biomedatasettingscreen opens this for some reason.
    }

    @Override
    protected Map<String, T> parseImpl(String str) { // TODO Listen to this.
        return new HashMap<>(0);
    }

    @Override
    protected boolean isValueValid(Map<String, T> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        //assert mc.world != null;
        NbtCompound valueTag = new NbtCompound();
        for (String biome : get().keySet()) {
            valueTag.put(biome,
                    get().get(biome).toTag());
        }
        tag.put("value", valueTag);
        return tag;
    }

    @Override
    protected Map<String, T> load(NbtCompound tag) { // FIXME
        //assert mc.world != null;
        get().clear();
        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            get().put(key, defaultData.get().copy().fromTag(valueTag.getCompound(key)));
        }

        return get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> extends SettingBuilder<BiomeDataSetting.Builder<T>, Map<String, T>, BiomeDataSetting<T>> {
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
