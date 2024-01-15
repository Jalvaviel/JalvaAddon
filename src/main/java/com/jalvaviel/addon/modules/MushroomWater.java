package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.systems.modules.Module;

public class MushroomWater extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<SettingColor> waterColor = sgGeneral.add(new ColorSetting.Builder()
        .name("water-color")
        .description("The color of the water.")
        .defaultValue(new SettingColor(0, 0, 0))
        .onChanged(val -> reload())
        .build()
    );
    public final Setting<SettingColor> skyColor = sgGeneral.add(new ColorSetting.Builder()
        .name("sky-color")
        .description("The color of the sky.")
        .defaultValue(new SettingColor(0, 0, 0))
        .onChanged(val -> reload())
        .build()
    );
    private void reload() {
        if (mc.worldRenderer != null && isActive()) mc.worldRenderer.reload();
    }
    public MushroomWater() {
        super(Addon.CATEGORY, "Mushroom Water", "Changes the water color on Mushroom biomes.");
    }
}
