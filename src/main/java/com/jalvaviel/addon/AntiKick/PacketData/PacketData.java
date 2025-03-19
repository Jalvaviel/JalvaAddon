package com.jalvaviel.addon.AntiKick.PacketData;

import com.jalvaviel.addon.AntiKick.AntiKickData.PacketDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeDataScreen;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import com.jalvaviel.addon.modules.BiomeColorChanger;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.IScreenFactory;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PacketData implements ICopyable<PacketData>, ISerializable<PacketData>, IChangeable, IPacketData<PacketData>, IScreenFactory {
    public int maxPackets;
    public int packetInterval;

    private boolean changed;

    public PacketData(int maxPackets, int packetInterval) {
        this.maxPackets = maxPackets;
        this.packetInterval = packetInterval;
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme, Class<? extends Packet<?>> packet, PacketDataSetting<PacketData> setting) { // When You access through specific BiomeData
        return new PacketDataScreen(theme, this, packet, setting);
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme) {
        return new PacketDataScreen(theme, this, null, null);
    }


    @Override
    public boolean isChanged() {
        return changed;
    }

    public void changed() {
        changed = true;
    }

    @Override
    public PacketData set(PacketData value) {
        maxPackets = value.maxPackets;
        packetInterval = value.packetInterval;

        changed = value.changed;

        return this;
    }

    @Override
    public PacketData copy() {
        return new PacketData(maxPackets, packetInterval);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putInt("maxPackets", maxPackets);
        tag.putInt("packetInterval", packetInterval);

        tag.putBoolean("changed", changed);

        return tag;
    }

    @Override
    public PacketData fromTag(NbtCompound tag) {
        maxPackets = tag.getInt("maxPackets");
        packetInterval = tag.getInt("packetInterval");

        changed = tag.getBoolean("changed");

        return this;
    }
}
