package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.utils.BlockUtils;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Objects;

import static com.jalvaviel.addon.Addon.LOG;
import static com.jalvaviel.addon.utils.BlockUtils.getBlockBreakingTimeMS;
import static meteordevelopment.meteorclient.systems.modules.player.AutoTool.isTool;

public class FastBreaker extends Module {
    SettingGroup sgGeneral = settings.getDefaultGroup();

    private static int packetCounter = 0;
    private static BlockPos miningBlock;
    RegistryEntry<Enchantment> enchantment;

    private final Setting<Integer> maxDistance = sgGeneral.add(new IntSetting.Builder()
        .name("max-distance")
        .description("The reach distance to break a block.")
        .defaultValue(5)
        .range(1, 10)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Integer> packetLimit = sgGeneral.add(new IntSetting.Builder()
        .name("packet-limit")
        .description("The packet limit to send block mining packets.")
        .defaultValue(10)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Double> instaMineThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("insta-mine-threshold")
        .description("The threshold to mine instantly")
        .defaultValue(80)
        .range(1, 150)
        .sliderRange(1, 150)
        .build()
    );

    private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown")
        .description("Block break cooldown in ticks.")
        .defaultValue(6)
        .min(0)
        .sliderMax(40)
        .build()
    );

    public FastBreaker() {
        super(Addon.CATEGORY, "fast-breaker", "Changes the delay between breaking blocks.");
    }
    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        assert mc.player != null;
        if (mc.player.age % cooldown.get() == 0 && packetCounter > 0) {
            packetCounter--;
        }
    }
    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        assert mc.player != null;
        assert mc.world != null;
        enchantment = mc.player.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get();
        HitResult hitResult = lookingAt();
        if (hitResult == null) return;
        BlockPos targetPos = lookingAt().getBlockPos();
        if (canMine(targetPos)) {
            ItemStack bestTool = mc.player.getInventory().getStack(getBestToolSlot(targetPos));
            int selectedSlot = mc.player.getInventory().selectedSlot;
            if (mc.player.getInventory().getStack(selectedSlot) != bestTool) {
                mc.player.getInventory().selectedSlot = mc.player.getInventory().getSlotWithStack(bestTool);
                Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().getSlotWithStack(bestTool)));
                packetCounter++;
            }
            mineBlock(targetPos);
            event.cancel();
        }
    }
/*
    @EventHandler
    private void onClick(MouseButtonEvent event) {
        if (mc.options.attackKey.isPressed()) {
            LOG.info("a");
        //if (event.action == KeyAction.Repeat || event.action == KeyAction.Press) {

        }
    }
 */
    private void mineBlock(BlockPos blockPos) {
        assert mc.getNetworkHandler() != null;
        double breakingTime = getBlockBreakingTimeMS(mc.player.getInventory().getMainHandStack(), blockPos, mc.player, mc.world, enchantment);
        miningBlock = blockPos;
        // Different mine packets for hardness values
        if (breakingTime <= instaMineThreshold.get()) {
            if (breakingTime > 50) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
            } else {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            }
        } else if (breakingTime > instaMineThreshold.get()) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, Direction.UP));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
        }
        if (packetLimit.get() != -1) {
            if (breakingTime > 50) {
                packetCounter += 3;
            } else {
                packetCounter++;
            }
        }
        miningBlock = null;
    }

    private BlockHitResult lookingAt() {
        HitResult blockHitResult = mc.crosshairTarget;
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            return (BlockHitResult) blockHitResult;
        }
        return null;
    }

    public int getBestToolSlot(BlockPos blockPos) {
        if (mc.player == null || mc.world == null) return -1;
        int bestTool = mc.player.getInventory().selectedSlot;
        for (int slotNum = 0; slotNum < 9; slotNum++) {
            if (BlockUtils.getBlockBreakingTimeMS(mc.player.getInventory().getStack(slotNum)
                , blockPos, mc.player, mc.world, enchantment) < BlockUtils.getBlockBreakingTimeMS(mc.player.getInventory().getStack(bestTool)
                , blockPos, mc.player, mc.world, enchantment)) {
                bestTool = slotNum;
            }
        }
        return bestTool;
    }

    private boolean canMine(BlockPos blockPos) {
        assert mc.world != null;
        assert mc.player != null;
        int bestToolSlot = getBestToolSlot(blockPos);
        ItemStack bestTool = mc.player.getInventory().getStack(bestToolSlot);
        if (miningBlock != null) return false;
        //if (miningBlock == blockPos) return false;
        //if (bestTool.getItem() != mc.player.getInventory().getStack(miningBlock.tool).getItem()) return false;
        double timeToMine = getBlockBreakingTimeMS(bestTool,blockPos,mc.player,mc.world,enchantment);

        if (timeToMine > 10000) {
            return false;
        }
        int packets = 1;
        if (timeToMine > 50) {
            packets = 3;
        }
        if (packetCounter < packetLimit.get() && packetCounter + packets < packetLimit.get()){
            miningBlock = blockPos;
            return true;
        }

        return false;
    }
}
