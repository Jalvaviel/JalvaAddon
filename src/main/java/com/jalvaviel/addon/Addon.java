package com.jalvaviel.addon;
import com.jalvaviel.addon.modules.*;
import com.jalvaviel.addon.utils.JalvaAddonSettingsWidgetFactory;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.registry.*;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static meteordevelopment.meteorclient.MeteorClient.mc;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String MOD_ID = "jalva-addon";
    public static final Category CATEGORY = new Category("Jalva Addons");
    public static final HudGroup HUD_GROUP = new HudGroup("Jalva Addons");

    @Override
    public void onInitialize() {
        // Framed canvas atlas generator

        // Modules
        //Modules.get().add(new MapDownloader());
        Modules.get().add(new MapBoundaries());
        Modules.get().add(new MushroomBiomeColors());
        Modules.get().add(new FastBreaker());
        Modules.get().add(new ElytraBoostPlus());
        Modules.get().add(new BiomeColorChanger());
        //Optional<RegistryEntryLookup<Biome>> regbiome = BuiltinRegistries.createWrapperLookup().createRegistryLookup().getOptional(RegistryKeys.BIOME);
        //Stream<RegistryKey<? extends Registry<?>>> registries = BuiltinRegistries.createWrapperLookup().streamAllRegistryKeys();
        //Stream<RegistryEntryLookup<Biome>> regbiome2 = regbiome.stream();
        LOG.info("AAAAAAAAAA");
        //JalvaAddonSettingsWidgetFactory widgetFactory = new JalvaAddonSettingsWidgetFactory();

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
