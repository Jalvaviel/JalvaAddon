package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.ElytraExtras;
import com.jalvaviel.addon.utils.getUndergroundDuck;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;


@Mixin(ESPBlock.class)
public class ESPBlockRenderMixin {

    @Final
    @Shadow
    private int x, y, z;

    @Inject(method = "render(Lmeteordevelopment/meteorclient/events/render/Render3DEvent;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRender(Render3DEvent event, CallbackInfo ci) {
        boolean isUndergroundActive;
        try {
            isUndergroundActive = ((getUndergroundDuck) Modules.get().get(BlockESP.class)).jalvaAddon$getUnderground();
        } catch (Exception e) {
            isUndergroundActive = false;
        }
        if (y >= mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z)-1 && isUndergroundActive) {
            ci.cancel();
        }
    }
}
