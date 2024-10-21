package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.BiomeColorChanger;
import com.jalvaviel.addon.utils.VanillaBiomesRegKeys;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BiomeKeys.class)
public class BiomeKeysMixin {
    @Inject(method = "register", at = @At("HEAD"))
    private static void register(String name, CallbackInfoReturnable<RegistryKey<Biome>> cir) {
        VanillaBiomesRegKeys vanillaBiomesRegKeys = VanillaBiomesRegKeys.getInstance();
        vanillaBiomesRegKeys.add(RegistryKey.of(RegistryKeys.BIOME, Identifier.ofVanilla(name)));
    }
}
