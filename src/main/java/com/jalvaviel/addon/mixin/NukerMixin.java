package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.BlockReplacer.ReplaceMode;
import com.jalvaviel.addon.modules.BlockReplacer;
import com.jalvaviel.addon.utils.getUndergroundDuck;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.entity.player.PlayerInventory.OFF_HAND_SLOT;

@Mixin(Nuker.class)
public class NukerMixin {
    @Shadow
    @Final
    private SettingGroup sgGeneral;

    @Unique
    private Setting<Boolean> underground;

    @Shadow
    private final List<BlockPos> blocks = new ArrayList<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectNewSetting(CallbackInfo ci) {
        underground = sgGeneral.add(new BoolSetting.Builder()
            .name("underground")
            .description("Renders blocks underground.")
            .defaultValue(true)
            .build());
    }

    /*
    @Inject(method = "lambda$onTickPre$9(DDDDLnet/minecraft/util/math/Box;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at =
    @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), cancellable = true)
    private void onBreakBlockUnderground(double pX, double pY, double pZ, double rangeSq, Box box, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        if (underground.get() && blockPos.getY() >= mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, blockPos.getX(), blockPos.getZ())-1 ) {
            ci.cancel();
        }
    }

    @Inject(method = "lambda$onTickPre$13(DDD)V", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"), remap = false)
    private void onReplaceBlock(double pX, double pY, double pZ, CallbackInfo ci) {
        BlockReplacer blockReplacer = Modules.get().get(BlockReplacer.class);
        if (blockReplacer.isActive() && blockReplacer.replaceMode.get() == ReplaceMode.NukerReplace) blockReplacer.replacePositions.addAll(blocks);//blockReplacer.replacePositions.add(blockPos);
    }

     */
}

