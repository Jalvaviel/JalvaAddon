package com.jalvaviel.addon.utils;

import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSettingScreen;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSetting;
import com.jalvaviel.addon.BiomeESP.BiomeList.BiomeListSettingScreen;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeData;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeDataScreen;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.BlockDataSettingScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;

import java.util.Collection;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory.registerCustomFactory;

public class JalvaAddonSettingsWidgetFactory{
    public JalvaAddonSettingsWidgetFactory() {
        registerCustomFactory(BiomeDataSetting.class, (theme) -> (table, setting) -> biomeDataW(table, (BiomeDataSetting) setting, theme));
        registerCustomFactory(BiomeListSetting.class, (theme) -> (table, setting) -> biomeListW(table, (BiomeListSetting) setting, theme));
    }

    private void biomeDataW(WTable table, BiomeDataSetting setting, GuiTheme theme) {
        WButton button = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().widget();
        button.action = () -> mc.setScreen(new BiomeDataSettingScreen(theme, setting));

        reset(table, setting, theme,null);
    }

    private void biomeListW(WTable table, BiomeListSetting setting, GuiTheme theme) {
        selectW(table, setting, theme, () -> mc.setScreen(new BiomeListSettingScreen(theme, setting)));
    }

    private void selectW(WContainer c, Setting<?> setting, GuiTheme theme, Runnable action) {
        boolean addCount = WSelectedCountLabel.getSize(setting) != -1;

        WContainer c2 = c;
        if (addCount) {
            c2 = c.add(theme.horizontalList()).expandCellX().widget();
            ((WHorizontalList) c2).spacing *= 2;
        }

        WButton button = c2.add(theme.button("Select")).expandCellX().widget();
        button.action = action;

        if (addCount) c2.add(new WSelectedCountLabel(setting).color(theme.textSecondaryColor()));

        reset(c, setting, theme, null);
    }

    private void reset(WContainer c, Setting<?> setting, GuiTheme theme, Runnable action) {
        WButton reset = c.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();
            if (action != null) action.run();
        };
    }

    private static class WSelectedCountLabel extends WMeteorLabel {
        private final Setting<?> setting;
        private int lastSize = -1;

        public WSelectedCountLabel(Setting<?> setting) {
            super("", false);

            this.setting = setting;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            int size = getSize(setting);

            if (size != lastSize) {
                set("(" + size + " selected)");
                lastSize = size;
            }

            super.onRender(renderer, mouseX, mouseY, delta);
        }

        public static int getSize(Setting<?> setting) {
            if (setting.get() instanceof Collection<?> collection) return collection.size();
            if (setting.get() instanceof Map<?, ?> map) return map.size();

            return -1;
        }
    }

}
