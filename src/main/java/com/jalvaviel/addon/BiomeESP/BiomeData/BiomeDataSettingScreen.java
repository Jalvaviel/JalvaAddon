package com.jalvaviel.addon.BiomeESP.BiomeData;

import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.jalvaviel.addon.modules.BiomeColorChanger.FALLBACK_KEYS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeDataSettingScreen extends WindowScreen {

    private final BiomeDataSetting<?> setting;
    List<RegistryKey<Biome>> BIOMES = new ArrayList<>();
    private WTable table;
    private String filterText = "";

    public BiomeDataSettingScreen(GuiTheme theme, BiomeDataSetting<?> setting) {
        super(theme, "Configure Biomes");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    public <T extends ICopyable<T> & ISerializable<T> & IChangeable & IBiomeData<T>> void initTable() {
        for (RegistryKey<Biome> biome : FALLBACK_KEYS) {
            T biomeData = (T) setting.get().get(biome);
            if (biomeData != null && biomeData.isChanged()) BIOMES.addFirst(biome);
            else BIOMES.add(biome);
        }

        for (RegistryKey<Biome> biome : BIOMES) {
            if (!StringUtils.containsIgnoreCase(biome.getValue().toString(), filterText)) continue;

            T biomeData = (T) setting.get().get(biome);
            table.add(theme.label(biome.getValue().toString())).expandCellX();
            table.add(theme.label((biomeData != null && biomeData.isChanged()) ? "*" : " "));

            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                T data = biomeData;
                if (data == null) data = (T) setting.defaultData.get().copy();
                mc.setScreen(data.createScreen(theme, biome, (BiomeDataSetting<T>) setting));
            };

            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                setting.get().remove(biome);
                setting.onChanged();

                if (biomeData != null && biomeData.isChanged()) {
                    table.clear();
                    initTable();
                }
            };

            table.row();
        }

        BIOMES.clear();
    }
}
