package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.MushroomBiomeColors;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.world.biome.BiomeKeys.*;


@Mixin(BiomeColors.class)
public class MushroomWaterMixin {
    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private static void onGetWaterColor(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        MushroomBiomeColors mushroom_water = Modules.get().get(MushroomBiomeColors.class);
        if (mushroom_water.isActive()) {
            assert mc.world != null;
            if (mc.world.getBiome(pos).matchesKey(MUSHROOM_FIELDS)) {
                info.setReturnValue(mushroom_water.waterColor.get().getPacked());
            } else {
                if (mc.world.getBiome(pos).matchesKey(DEEP_OCEAN) || mc.world.getBiome(pos).matchesKey(DEEP_COLD_OCEAN) ||
                mc.world.getBiome(pos).matchesKey(DEEP_FROZEN_OCEAN) || mc.world.getBiome(pos).matchesKey(DEEP_LUKEWARM_OCEAN)){
                    info.setReturnValue(mushroom_water.deepOceanWaterColor.get().getPacked());
                }
            }
        }
    }
}

