package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.BiomeColorChanger;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;


@Mixin(BiomeColors.class)
public class GrassColorMixin {
    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColor(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        BiomeColorChanger biomeColorChanger = Modules.get().get(BiomeColorChanger.class);
        if (biomeColorChanger.isActive()) {
            assert mc.world != null;
            assert mc.player != null;
            try {
                RegistryEntry<Biome> currentBiome = mc.world.getBiome(pos);
                if (biomeColorChanger.biomes.get().contains(currentBiome.value())) {
                    info.setReturnValue(biomeColorChanger.biomeConfigs.get().get(currentBiome.value()).grassColor.getPacked());
                }
            } catch (Exception ignored) {}
        }
    }
}

