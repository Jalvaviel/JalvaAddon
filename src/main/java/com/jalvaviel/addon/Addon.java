package com.jalvaviel.addon;
import com.jalvaviel.addon.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Jalva Addons");
    public static final HudGroup HUD_GROUP = new HudGroup("Jalva Addons");

    @Override
    public void onInitialize() {
        // Modules
        Modules.get().add(new MapDownloader());
        Modules.get().add(new MapBoundaries());
        //Modules.get().add(new MapStorage());
        Modules.get().add(new MushroomWater());

        // Commands
        //Commands.add(new CommandExample());

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
