package com.jalvaviel.addon.BiomeESP.ESPBiomeData;

import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ESPBiomeData implements ICopyable<ESPBiomeData>, ISerializable<ESPBiomeData>, IChangeable, IBiomeData<ESPBiomeData>, IScreenFactory {
    public SettingColor waterColor;
    public SettingColor skyColor;
    public SettingColor foliageColor;
    public SettingColor grassColor;
    private boolean changed;

    public ESPBiomeData(SettingColor waterColor, SettingColor skyColor, SettingColor foliageColor, SettingColor grassColor) {
        this.waterColor = waterColor;
        this.skyColor = skyColor;
        this.foliageColor = foliageColor;
        this.grassColor = grassColor;
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme, RegistryKey<Biome> biome, BiomeDataSetting<ESPBiomeData> setting) { // When You access through specific BiomeData
        return new ESPBiomeDataScreen(theme, this, biome, setting);
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme) { // When You access through default generic undefined biome FIXME only reads grassColor
        return new ESPBiomeDataScreen(theme, this, null, null);
    }


    @Override
    public boolean isChanged() {
        return changed;
    }

    public void changed() {
        changed = true;
        if (mc.worldRenderer != null && Modules.get().isActive(BiomeColorChanger.class)) mc.worldRenderer.reload();
    }

    public void tickRainbow() {
        waterColor.update();
        skyColor.update();
        foliageColor.update();
        grassColor.update();
    }

    @Override
    public ESPBiomeData set(ESPBiomeData value) {
        waterColor.set(value.waterColor);
        skyColor.set(value.skyColor);
        foliageColor.set(value.foliageColor);
        grassColor.set(value.grassColor);

        changed = value.changed;

        return this;
    }

    @Override
    public ESPBiomeData copy() {
        return new ESPBiomeData(new SettingColor(waterColor), new SettingColor(skyColor), new SettingColor(foliageColor), new SettingColor(grassColor));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("waterColor", waterColor.toTag());
        tag.put("skyColor", skyColor.toTag());
        tag.put("foliageColor", foliageColor.toTag());
        tag.put("grassColor", grassColor.toTag());

        tag.putBoolean("changed", changed);

        return tag;
    }

    @Override
    public ESPBiomeData fromTag(NbtCompound tag) {
        waterColor.fromTag(tag.getCompound("waterColor"));
        skyColor.fromTag(tag.getCompound("skyColor"));
        foliageColor.fromTag(tag.getCompound("foliageColor"));
        grassColor.fromTag(tag.getCompound("grassColor"));

        changed = tag.getBoolean("changed");

        return this;
    }
}
