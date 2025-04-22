package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.utils.getUndergroundDuck;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;


@Mixin(BlockESP.class)
public class BlockESPMixin implements getUndergroundDuck {
    @Shadow @Final
    private SettingGroup sgGeneral;

    @Unique
    private Setting<Boolean> underground;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectNewSetting(CallbackInfo ci) {
        underground = sgGeneral.add(new BoolSetting.Builder()
            .name("underground")
            .description("Renders blocks underground.")
            .defaultValue(true)
            .build());
    }

    @Override
    public boolean jalvaAddon$getUnderground() {
        return underground.get();
    }
}
