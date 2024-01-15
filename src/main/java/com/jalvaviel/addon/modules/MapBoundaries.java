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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

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
    private final Setting<Boolean> oclussion = sgGeneral.add(new BoolSetting.Builder()
        .name("Oclussion")
        .description("Hide the faces that are covered by blocks")
        .defaultValue(true)
        .build()
    );

    public MapBoundaries() {
        super(Addon.CATEGORY, "Map Boundaries", "Overlays the current map boundaries");
    }

    private final Mesh mesh = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Triangles, Mesh.Attrib.Vec3, Mesh.Attrib.Color);

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
        // mesh.depthTest = true;
        // mesh.color(color2);
        // mesh.begin();
        // mesh.quad(posX1, bottomY, posX2, topY);
        // mesh.end();
        // mesh.render(event.matrices);
    }
}
