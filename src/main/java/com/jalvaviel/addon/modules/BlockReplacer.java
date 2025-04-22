package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.BlockReplacer.ReplaceMode;
import com.jalvaviel.addon.ChunkTrailer.Enums.ReplayMode;
import meteordevelopment.meteorclient.events.entity.player.PlaceBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static com.jalvaviel.addon.BlockReplacer.ReplaceMode.NukerReplace;
import static net.minecraft.entity.player.PlayerInventory.OFF_HAND_SLOT;

public class BlockReplacer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in ticks.")
        .sliderRange(1,200)
        .defaultValue(10)
        .build()
    );

    public final Setting<ReplaceMode> replaceMode = sgGeneral.add(new EnumSetting.Builder<ReplaceMode>()
        .name("replace-mode")
        .description("Mode of replace.")
        .defaultValue(NukerReplace)
        .build()
    );

    private final Setting<Block> blockToReplace = sgGeneral.add(new BlockSetting.Builder()
        .name("block-to-replace")
        .description("The block to be replaced for.")
        .defaultValue(Blocks.MYCELIUM)
        .visible(() -> replaceMode.get() == NukerReplace)
        .build());

    public BlockReplacer() {
        super(Addon.CATEGORY, "block-replacer", "AutoClicker sucks ass.");
    }

    private Block currentBlock;
    public BlockPos currentBlockPos;

    private int counter = 0;
    private Nuker nuker = Modules.get().get(Nuker.class);

    public List<BlockPos> replacePositions = new ArrayList<>();

/*
    @EventHandler
    private void onPlaceBlock(PlaceBlockEvent event) {
        if (currentBlock != event.block) currentBlock = event.block;
        if (currentBlockPos != event.blockPos) currentBlockPos = event.blockPos;
    }

 */

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (mc.currentScreen instanceof ModulesScreen) return;
        if (delay.get() == 0) return;
        counter++;
        if (counter <= delay.get()) return;
        switch (replaceMode.get()) {
            case Basic -> {
                if (currentBlock == null || currentBlockPos == null || mc.world == null) return;
                if (mc.world.getBlockState(currentBlockPos).getBlock() != currentBlock && mc.world.getBlockState(currentBlockPos).isReplaceable()) {
                    FindItemResult blockItem = InvUtils.find(currentBlock.asItem());
                    if (!blockItem.found() || !PlayerUtils.isWithinReach(currentBlockPos)) toggle();
                    InvUtils.move().from(blockItem.slot()).to(mc.player.getInventory().selectedSlot);
                    BlockUtils.place(currentBlockPos, blockItem, true, 0, true);
                }
            }
            case NukerReplace -> {
                if (!replacePositions.isEmpty()) {
                    BlockPos replacePos = replacePositions.getFirst();
                    FindItemResult blockItem = InvUtils.find(blockToReplace.get().asItem());
                    if (!blockItem.found() || !PlayerUtils.isWithinReach(replacePos)) return;
                    if (nuker.isActive()) Modules.get().get(Nuker.class).toggle();
                    //if (!blockItem.isOffhand()) InvUtils.move().from(blockItem.slot()).toOffhand();
                    if (BlockUtils.place(replacePos, blockItem, true, 0, true) && !nuker.isActive()) {
                        replacePositions.removeFirst();
                        if(replacePositions.isEmpty()) nuker.toggle();
                    }

                }
                /*
                if (nuker.isActive()) Modules.get().get(Nuker.class).toggle();
                if (currentBlockPos == null || mc.world == null) return;
                if (!mc.world.getBlockState(currentBlockPos).isReplaceable()) return;
                FindItemResult blockItem = InvUtils.find(blockToReplace.get().asItem());
                if (!blockItem.found() || !PlayerUtils.isWithinReach(currentBlockPos)) return;
                if (BlockUtils.place(currentBlockPos, blockItem, true, 0, true) && !nuker.isActive()) nuker.toggle();
                 */
            }
        }
        counter = 0;
    }

    @EventHandler
    public void onDeactivate() {
        currentBlock = null;
        currentBlockPos = null;
        counter = 0;
        replacePositions.clear();
    }
}
