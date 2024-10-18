package com.jalvaviel.addon.BiomeESP.ESPBiomeData;

import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.world.biome.Biome;

public interface IBiomeData<T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> {
    WidgetScreen createScreen(GuiTheme theme, Biome biome, BiomeDataSetting<T> setting);
}
