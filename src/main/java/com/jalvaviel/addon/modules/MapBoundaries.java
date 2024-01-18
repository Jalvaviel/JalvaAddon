package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.*;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

public class MapBoundaries extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<SettingColor> outlineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Outline Color")
        .description("The color of the outline")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("The color of the side")
        .defaultValue(new SettingColor(255, 0, 0, 64))
        .build()
    );
    private final Setting<Boolean> occlusion = sgGeneral.add(new BoolSetting.Builder()
        .name("Occlusion")
        .description("Hide the faces that are covered by blocks")
        .defaultValue(true)
        .build()
    );

    public MapBoundaries() {
        super(Addon.CATEGORY, "Map Boundaries", "Overlays the current map boundaries");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Color color1 = outlineColor.get().copy();
        Color color2 = sideColor.get().copy();
        int posX1 = (int) (Math.round(mc.player.getX() / 128) * 128 - 64);
        int posZ1 = (int) (Math.round(mc.player.getZ() / 128) * 128 - 64);
        int posX2 = posX1 + 128;
        int posZ2 = posZ1 + 128;
        int bottomY = mc.world.getBottomY();
        int topY = mc.world.getTopY();
        event.renderer.triangles.depthTest = occlusion.get();
        event.renderer.lines.depthTest = occlusion.get();
        event.renderer.box(posX1+ 0.0075,bottomY,posZ1 + 0.0075,posX2 - 0.0075,topY,posZ2 - 0.0075,color2,color1,ShapeMode.Both,6);
        // Z fighting sucks ass.
    }
}
