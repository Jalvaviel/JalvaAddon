package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.BiomeColorChanger;
import com.jalvaviel.addon.modules.MushroomBiomeColors;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.world.biome.BiomeKeys.*;

@Mixin(ClientWorld.class)
public class SkyColorMixin {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        BiomeColorChanger biomeColorChanger = Modules.get().get(BiomeColorChanger.class);
        if (biomeColorChanger.isActive()) {
            assert mc.world != null;
            assert mc.player != null;
            try {
                RegistryEntry<Biome> currentBiome = mc.world.getBiome(mc.player.getBlockPos());
                if (biomeColorChanger.biomes.get().contains(currentBiome.value())) {
                    Vec3d returnValue = biomeColorChanger.biomeConfigs.get().get(currentBiome.value()).skyColor.getVec3d();
                    info.setReturnValue(returnValue);
                }
            } catch (Exception ignored) {}
        }
    }
}
