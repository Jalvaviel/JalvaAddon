package com.jalvaviel.addon;
import com.jalvaviel.addon.AntiKick.AntiKickData.PacketDataSetting;
import com.jalvaviel.addon.AntiKick.AntiKickData.PacketDataSettingScreen;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSettingScreen;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSettingScreen;
import com.jalvaviel.addon.hud.ImageHud;
import com.jalvaviel.addon.modules.ElytraExtras;
import com.jalvaviel.addon.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.builtin.HudTab;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import org.slf4j.Logger;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;


import static meteordevelopment.meteorclient.MeteorClient.mc;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String MOD_ID = "jalvaaddon";
    public static final Category CATEGORY = new Category("Jalva Addons");
    public static final HudGroup HUD_GROUP = new HudGroup("Jalva Addons");
    @Override
    public void onInitialize() {
        SettingsWidgetFactory.registerCustomFactory(BiomeListSetting.class, (theme) -> (table, setting) -> {
            WButton button = table.add(theme.button("Select")).expandCellX().widget();
            button.action = () -> mc.setScreen(new BiomeListSettingScreen(theme, (BiomeListSetting) setting));
            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = setting::reset;
        });

        SettingsWidgetFactory.registerCustomFactory(BiomeDataSetting.class, (theme) -> (table, setting) -> {
            WButton button = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().widget();
            button.action = () -> mc.setScreen(new BiomeDataSettingScreen(theme, (BiomeDataSetting<?>) setting));
            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = setting::reset;
        });

        SettingsWidgetFactory.registerCustomFactory(PacketDataSetting.class, (theme) -> (table, setting) -> {
            WButton button = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().widget();
            button.action = () -> mc.setScreen(new PacketDataSettingScreen(theme, (PacketDataSetting<?>) setting));
            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = setting::reset;
        });

        // Modules
        Modules.get().add(new MapBoundaries());
        Modules.get().add(new ElytraExtras());
        Modules.get().add(new BiomeColorChanger());
        Modules.get().add(new ChunkTrailer());
        Modules.get().add(new BlockReplacer());
        Modules.get().add(new AntiKick());
        Modules.get().add(new ItemESP());
        //Modules.get().add(new ImageOverlay());
        Hud.get().register(ImageHud.INFO);

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
