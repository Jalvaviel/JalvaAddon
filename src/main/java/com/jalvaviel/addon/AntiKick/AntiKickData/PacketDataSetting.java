package com.jalvaviel.addon.AntiKick.AntiKickData;

import com.jalvaviel.addon.AntiKick.PacketData.IPacketData;
import com.jalvaviel.addon.AntiKick.PacketData.PacketData;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PacketDataSetting<T extends ICopyable<T> & ISerializable<T> & IChangeable & IPacketData<T>> extends Setting<Map<Class<? extends Packet<?>>, T>> {
    public final IGetter<T> defaultData;

    public PacketDataSetting(String name, String description, Map<Class<? extends Packet<?>>,T> defaultValue,
                             Consumer<Map<Class<? extends Packet<?>>, T>> onChanged, Consumer<Setting<Map<Class<? extends Packet<?>>, T>>>
                                 onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.defaultData = defaultData;
    }

    @Override
    public void resetImpl() {
        value = new HashMap<>(defaultValue);
    }

    @Override
    protected Map<Class<? extends Packet<?>>, T> parseImpl(String str) {
        return new HashMap<>(0);
    }

    @Override
    protected boolean isValueValid(Map<Class<? extends Packet<?>>, T> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (Class<? extends Packet<?>> packet : get().keySet()) {
            valueTag.put(PacketUtils.getName(packet), get().get(packet).toTag());
        }
        tag.put("value", valueTag);
        return tag;
    }

    @Override
    protected Map<Class<? extends Packet<?>>, T> load(NbtCompound tag) {
        get().clear();
        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            get().put(PacketUtils.getPacket(key), defaultData.get().copy().fromTag(valueTag.getCompound(key))); //defaultData is a PacketData object (at least for now)
        }

        return get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IChangeable & IPacketData<T>> extends SettingBuilder<Builder<T>, Map<Class<? extends Packet<?>>, T>, PacketDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(new HashMap<>(0));
        }

        public Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override
        public PacketDataSetting<T> build() {
            return new PacketDataSetting<>(name, description, defaultValue, onChanged, onModuleActivated, defaultData, visible);
        }
    }
}
