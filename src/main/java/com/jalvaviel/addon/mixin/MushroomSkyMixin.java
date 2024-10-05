package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.MushroomBiomeColors;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.world.biome.BiomeKeys.*;

@Mixin(ClientWorld.class)
public class MushroomSkyMixin {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        MushroomBiomeColors mushroom_sky = Modules.get().get(MushroomBiomeColors.class);
        if (mushroom_sky.isActive()) {
            assert mc.world != null;
            if (mc.world.getBiome(mc.player.getBlockPos()).matchesKey(MUSHROOM_FIELDS)) {
                info.setReturnValue(mushroom_sky.skyColor.get().getVec3d());
            } else {
                if (mc.world.getBiome(mc.player.getBlockPos()).matchesKey(DEEP_OCEAN) || mc.world.getBiome(mc.player.getBlockPos()).matchesKey(DEEP_COLD_OCEAN) ||
                    mc.world.getBiome(mc.player.getBlockPos()).matchesKey(DEEP_FROZEN_OCEAN) || mc.world.getBiome(mc.player.getBlockPos()).matchesKey(DEEP_LUKEWARM_OCEAN)){
                    info.setReturnValue(mushroom_sky.deepOceanSkyColor.get().getVec3d());
                }
            }
        }
    }

}
