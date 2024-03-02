package com.jalvaviel.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static com.jalvaviel.addon.commands.Pos1.*;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Pos2 extends Command {
    public static int blockX2 = blockX1;
    public static int blockY2 = blockY1;
    public static int blockZ2 = blockZ1;

    public Pos2() {
        super("pos2", "Select a block position");
    }
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            HitResult hitResult = mc.crosshairTarget;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                BlockPos blockPos = entityHitResult.getEntity().getBlockPos();
                blockX2 = blockPos.getX();
                blockY2 = blockPos.getY();
                blockZ2 = blockPos.getZ();
            }
            return SINGLE_SUCCESS;
        });
    }
}
