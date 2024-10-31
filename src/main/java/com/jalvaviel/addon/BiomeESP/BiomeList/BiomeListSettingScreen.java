package com.jalvaviel.addon.BiomeESP.BiomeList;

import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import com.jalvaviel.addon.utils.VanillaBiomesRegKeys;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.*;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BiomeListSettingScreen extends WindowScreen {
    private final BiomeListSetting setting;
    List<String> BIOMES = VanillaBiomesRegKeys.getInstance().getBiomes();
    private WTable table;
    private String filterText = "";

    public BiomeListSettingScreen(GuiTheme theme, BiomeListSetting setting) {
        super(theme, "Select Biomes");
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
        for (String biome : BIOMES) {
            if (!StringUtils.containsIgnoreCase(biome, filterText)) continue;
            table.add(theme.label(biome)).expandCellX();
            WCheckbox biomeC = table.add(theme.checkbox(setting.get().contains(biome))).expandCellX().right().widget();
            biomeC.action = () -> {
                if (biomeC.checked) {
                    setting.get().add(biome);
                } else {
                    setting.get().remove(biome);
                }
                setting.onChanged();
            };
            table.row();
        }
    }
}

/*
    @Override
    protected boolean includeValue(RegistryKey<Biome> value) {
        Predicate<RegistryKey<Biome>> filter = ((BiomeListSetting) setting).filter;

        if (filter == null) return true;
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(RegistryKey<Biome> value) {
        return theme.label(getValueName(value));
    }

    @Override
    protected String getValueName(RegistryKey<Biome> value) {
        //assert mc.world != null;
        return Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getEntry(value).getIdAsString());
        /*
        if (mc.world == null) {
            Optional<RegistryEntry.Reference<Biome>> entry = BuiltinRegistries.createWrapperLookup().createRegistryLookup().getOptionalEntry(
                RegistryKeys.BIOME, RegistryEntry.of(value).getKey().get());
            return entry.orElseThrow().value().toString(); // Reference implements RegistryEntry, this is fine
        } else {
            return Objects.requireNonNull(mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(value)).toString();
        }


    }
 */

