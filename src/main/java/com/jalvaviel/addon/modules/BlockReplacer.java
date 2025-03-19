package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class BlockReplacer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks.")
        .sliderRange(1,200)
        .defaultValue(10)
        .build()
    );

    public BlockReplacer() {
        super(Addon.CATEGORY, "block-replacer", "AutoClicker sucks ass.");
    }

    private Block currentBlock;
    private BlockPos currentBlockPos;

    private int counter = 0;


    @EventHandler
    private void onPlaceBlock(PlaceBlockEvent event) {
        if (currentBlock != event.block) currentBlock = event.block;
        if (currentBlockPos != event.blockPos) currentBlockPos = event.blockPos;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (currentBlock == null || currentBlockPos == null || mc.world == null || mc.currentScreen instanceof ModulesScreen) return;
        if (delay.get() == 0) return;
        counter++;
        if (counter <= delay.get()) return;
        if (mc.world.getBlockState(currentBlockPos).getBlock() != currentBlock && mc.world.getBlockState(currentBlockPos).isReplaceable()) {
            FindItemResult blockItem = InvUtils.find(currentBlock.asItem());
            if (!blockItem.found() || !PlayerUtils.isWithinReach(currentBlockPos)) toggle();
            InvUtils.move().from(blockItem.slot()).to(mc.player.getInventory().selectedSlot);
            BlockUtils.place(currentBlockPos, blockItem, true, 0, true);
            counter = 0;
        }
    }

    @EventHandler
    public void onDeactivate() {
        currentBlock = null;
        currentBlockPos = null;
        counter = 0;
    }
}
