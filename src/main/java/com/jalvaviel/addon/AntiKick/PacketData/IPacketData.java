package com.jalvaviel.addon.AntiKick.PacketData;

import com.jalvaviel.addon.AntiKick.AntiKickData.PacketDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public interface IPacketData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IPacketData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Class<? extends Packet<?>> packet, PacketDataSetting<T> setting);
}
