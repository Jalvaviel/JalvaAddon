package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.utils.ColorUtils;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;

import java.util.List;

import static com.jalvaviel.addon.utils.ColorUtils.checkCorner;
import static java.lang.Double.MAX_VALUE;

public class ItemESP extends Module {
    public SettingGroup sgGeneral = settings.getDefaultGroup();
    public SettingGroup sgRender = settings.createGroup("Render");

    public ItemESP() {
        super(Addon.CATEGORY, "item-esp", "Renders items on ground from a certain type.");
    }

    private final Setting<List<Item>> items = this.sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to highlight.")
        .build()
    );

    private final Setting<ESP.Mode> mode = sgRender.add(new EnumSetting.Builder<ESP.Mode>()
        .name("mode")
        .description("Rendering mode.")
        .defaultValue(ESP.Mode.Box)
        .build()
    );

    private final Setting<Boolean> interpolateColors = sgGeneral.add(new BoolSetting.Builder()
        .name("interpolate-colors")
        .description("Interpolates the color according to the quantity of the thrown stack.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of the box.")
        .defaultValue(new SettingColor(255, 0, 0, 64))
        .build()
    );

    private final Setting<SettingColor> color2 = sgGeneral.add(new ColorSetting.Builder()
        .name("second-color")
        .description("The second color of the box for interpolation.")
        .defaultValue(new SettingColor(0, 255, 0, 64))
        .visible(interpolateColors::get)
        .build()
    );

    private final Setting<Boolean> occlusion = sgGeneral.add(new BoolSetting.Builder()
        .name("occlusion-culling")
        .description("Hide the faces that are covered by blocks.")
        .defaultValue(false)
        .build()
    );

    Vector3d pos1, pos2 = new Vector3d();

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mode.get() != ESP.Mode._2D) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof ItemEntity) {
                    try {
                        ItemStack item = (ItemStack) entity.getDataTracker().getChangedEntries().getFirst().value();
                        if (items.get().contains(item.getItem())) {
                            if (interpolateColors.get()) drawBoundingBox(event, entity, item);
                            else drawBoundingBox(event, entity, color.get());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.mode.get() == ESP.Mode._2D) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof ItemEntity) {
                    try {
                        ItemStack item = (ItemStack) entity.getDataTracker().getChangedEntries().getFirst().value();
                        if (items.get().contains(item.getItem())) {
                            if (interpolateColors.get()) drawBoundingBox(event, entity, item);
                            else drawBoundingBox(event, entity, color.get());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity, Color color) {
        event.renderer.lines.depthTest = occlusion.get();
        event.renderer.triangles.depthTest = occlusion.get();
        if (mode.get() == ESP.Mode.Wireframe) WireframeEntityRenderer.render(event, entity, 1.0F, color, color.copy().a(255), ShapeMode.Both);
        else {
            double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();
            Box box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, color, color.copy().a(255), ShapeMode.Both, 0);
        }
    }

    private void drawBoundingBox(Render2DEvent event, Entity entity, Color color) {
        if (mode.get() == ESP.Mode._2D) {
            //Renderer2D.COLOR.begin();
            Box box = entity.getBoundingBox();
            double x = MathHelper.lerp((double)event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp((double)event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp((double)event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();
            pos1 = new Vector3d(MAX_VALUE,MAX_VALUE,MAX_VALUE);//.set(MAX_VALUE,MAX_VALUE,MAX_VALUE);
            pos2 = new Vector3d(0,0,0);//.set(0.0F, 0.0F, 0.0F);
            if (!checkCorner(box.minX + x, box.minY + y, box.minZ + z, this.pos1, this.pos2) && !checkCorner(box.maxX + x, box.minY + y, box.minZ + z, this.pos1, this.pos2) && !checkCorner(box.minX + x, box.minY + y, box.maxZ + z, this.pos1, this.pos2) && !checkCorner(box.maxX + x, box.minY + y, box.maxZ + z, this.pos1, this.pos2) && !checkCorner(box.minX + x, box.maxY + y, box.minZ + z, this.pos1, this.pos2) && !checkCorner(box.maxX + x, box.maxY + y, box.minZ + z, this.pos1, this.pos2) && !checkCorner(box.minX + x, box.maxY + y, box.maxZ + z, this.pos1, this.pos2) && !checkCorner(box.maxX + x, box.maxY + y, box.maxZ + z, this.pos1, this.pos2)) {
                Renderer2D.COLOR.line(box.minX, box.minY, box.minX, box.maxY, color);
                Renderer2D.COLOR.line(box.maxX, box.minY, box.maxX, box.maxY, color);
                Renderer2D.COLOR.line(box.minX, box.minY, box.maxX, box.minY, color);
                Renderer2D.COLOR.line(box.minX, box.maxY, box.maxX, box.maxY, color);
                Renderer2D.COLOR.quad(box.minX, box.minY, box.maxX - box.minX, box.maxY - box.minY, color);

            }
            //Renderer2D.COLOR.render((MatrixStack)null);
        }
    }

    private void drawBoundingBox(Render2DEvent event, Entity entity, ItemStack itemStack) {
        Color lineColor = ColorUtils.getInterpolatedColor(color.get(),color2.get(),itemStack);
        drawBoundingBox(event,entity,lineColor);
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity, ItemStack itemStack) {
        Color lineColor = ColorUtils.getInterpolatedColor(color.get(),color2.get(),itemStack);
        drawBoundingBox(event,entity,lineColor);
    }
}
