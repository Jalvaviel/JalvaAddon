package com.jalvaviel.addon.utils;

import com.jalvaviel.addon.modules.MushroomBiomeColors;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;

import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.block.Block;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public class ESPBiomeGroup {
    /*
    private static final MushroomBiomeColors mushroomBiomeColors = Modules.get().get(MushroomBiomeColors.class);

    private final Block block;

    public final UnorderedArrayList<ESPBiomeBlock> blocks = new UnorderedArrayList<>();

    private double sumX, sumY, sumZ;

    public ESPBiomeGroup(Block block) {
        this.block = block;
    }

    public void add(ESPBiomeBlock block, boolean removeFromOld, boolean splitGroup) {
        blocks.add(block);
        sumX += block.x;
        sumY += block.y;
        sumZ += block.z;

        if (block.group != null && removeFromOld) block.group.remove(block, splitGroup);
        block.group = this;
    }

    public void add(ESPBiomeBlock block) {
        add(block, true, true);
    }

    public void remove(ESPBiomeBlock block, boolean splitGroup) {
        blocks.remove(block);
        sumX -= block.x;
        sumY -= block.y;
        sumZ -= block.z;

        if (blocks.isEmpty()) mushroomBiomeColors.removeGroup(block.group);
        else if (splitGroup) {
            trySplit(block);
        }
    }

    public void remove(ESPBiomeBlock block) {
        remove(block, true);
    }

    private void trySplit(ESPBiomeBlock block) {
        Set<ESPBiomeBlock> neighbours = new ObjectOpenHashSet<>(6);

        for (int side : ESPBiomeBlock.SIDES) {
            if ((block.neighbours & side) == side) {
                ESPBiomeBlock neighbour = block.getSideBlock(side);
                if (neighbour != null) neighbours.add(neighbour);
            }
        }
        if (neighbours.size() <= 1) return;

        Set<ESPBiomeBlock> remainingBlocks = new ObjectOpenHashSet<>(blocks);
        Queue<ESPBiomeBlock> blocksToCheck = new ArrayDeque<>();

        blocksToCheck.offer(blocks.get(0));
        remainingBlocks.remove(blocks.get(0));
        neighbours.remove(blocks.get(0));

        loop: {
            while (!blocksToCheck.isEmpty()) {
                ESPBiomeBlock b = blocksToCheck.poll();

                for (int side : ESPBiomeBlock.SIDES) {
                    if ((b.neighbours & side) != side) continue;
                    ESPBiomeBlock neighbour = b.getSideBlock(side);

                    if (neighbour != null && remainingBlocks.contains(neighbour)) {
                        blocksToCheck.offer(neighbour);
                        remainingBlocks.remove(neighbour);

                        neighbours.remove(neighbour);
                        if (neighbours.isEmpty()) break loop;
                    }
                }
            }
        }

        if (neighbours.size() > 0) {
            ESPBiomeGroup group = mushroomBiomeColors.newGroup(this.block);
            group.blocks.ensureCapacity(remainingBlocks.size());

            blocks.removeIf(remainingBlocks::contains);

            for (ESPBiomeBlock b : remainingBlocks) {
                group.add(b, false, false);

                sumX -= b.x;
                sumY -= b.y;
                sumZ -= b.z;
            }

            if (neighbours.size() > 1) {
                block.neighbours = 0;

                for (ESPBiomeBlock b : neighbours) {
                    int x = b.x - block.x;
                    if (x == 1) block.neighbours |= ESPBiomeBlock.RI;
                    else if (x == -1) block.neighbours |= ESPBiomeBlock.LE;

                    int y = b.y - block.y;
                    if (y == 1) block.neighbours |= ESPBiomeBlock.TO;
                    else if (y == -1) block.neighbours |= ESPBiomeBlock.BO;

                    int z = b.z - block.z;
                    if (z == 1) block.neighbours |= ESPBiomeBlock.FO;
                    else if (z == -1) block.neighbours |= ESPBiomeBlock.BA;
                }

                group.trySplit(block);
            }
        }
    }

    public void merge(ESPBiomeGroup group) {
        blocks.ensureCapacity(blocks.size() + group.blocks.size());
        for (ESPBiomeBlock block : group.blocks) add(block, false, false);
        mushroomBiomeColors.removeGroup(group);
    }

    public void render(Render3DEvent event) {
        ESPBlockData blockData = mushroomBiomeColors.getBlockData(block);

        if (blockData.tracer) {
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, sumX / blocks.size() + 0.5, sumY / blocks.size() + 0.5, sumZ / blocks.size() + 0.5, blockData.tracerColor);
        }
    }

     */
}
