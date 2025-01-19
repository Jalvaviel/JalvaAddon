package com.jalvaviel.addon;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSettingScreen;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSettingScreen;
import com.jalvaviel.addon.modules.*;
import com.jalvaviel.addon.utils.FileSetting;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;


import java.io.File;

import static meteordevelopment.meteorclient.MeteorClient.mc;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String MOD_ID = "jalva-addon";
    public static final Category CATEGORY = new Category("Jalva Addons");
    public static final HudGroup HUD_GROUP = new HudGroup("Jalva Addons");
    @Override
    public void onInitialize() {
        SettingsWidgetFactory.registerCustomFactory(BiomeListSetting.class, (theme) -> (table, setting) -> {
            WButton button = table.add(theme.button("Select")).expandCellX().widget();
            button.action = () -> mc.setScreen(new BiomeListSettingScreen(theme, (BiomeListSetting) setting));
            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                setting.reset();
            };
        });

        SettingsWidgetFactory.registerCustomFactory(BiomeDataSetting.class, (theme) -> (table, setting) -> {
            WButton button = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().widget();
            button.action = () -> mc.setScreen(new BiomeDataSettingScreen(theme, (BiomeDataSetting<?>) setting));
            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                setting.reset();
            };
        });


        // Modules
        Modules.get().add(new MapBoundaries());
        Modules.get().add(new FastBreaker());
        Modules.get().add(new ElytraBoostPlus());
        Modules.get().add(new BiomeColorChanger());
        Modules.get().add(new ChunkTrailer());
        //Modules.get().add(new SpawnerFarm());

        // Commands
        //Commands.add(new Pos1());
        //Commands.add(new Pos2());
        // selectW(table, setting, () -> mc.setScreen(new BlockListSettingScreen(theme, setting)));

        // HUD
        //Hud.get().register(HudExample.INFO);
    }
    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.jalvaviel.addon";
    }
}
