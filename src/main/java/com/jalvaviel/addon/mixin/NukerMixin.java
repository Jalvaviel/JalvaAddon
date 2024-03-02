package com.jalvaviel.addon.mixin;

import com.jalvaviel.addon.modules.MushroomWater;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static net.minecraft.world.biome.BiomeKeys.MUSHROOM_FIELDS;


@Mixin(Nuker.class)
public class NukerMixin {
    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void injectBreakBlock(BlockPos blockPos, CallbackInfo info) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos)));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos)));
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos)));
        ChatUtils.sendMsg(Text.of("Sent 3 Packets: "+mc.world.getBlockState(blockPos).getBlock().toString()));
    }
}
