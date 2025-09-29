package com.jalvaviel.addon.modules.old;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.*;

public class Extrajump extends Module {
    public Extrajump()  {
        super(Addon.CATEGORY, "extra-jump", "Makes you jump 1.5 blocks with the 1e-7 exploit.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Vec3d playerPos = mc.player.getPos();
        float yaw = Math.round((mc.player.getYaw() + 1f) / 90f) * 90f;
        double dx = -sin(Math.toRadians(yaw));
        double dz = cos(Math.toRadians(yaw));
        BlockPos target = new BlockPos((int) (playerPos.getX()+dx),(int) playerPos.getY(), (int) (playerPos.getZ()+dz));
        boolean isNextBlock = mc.world.getBlockState(target).getCollisionShape(mc.world, target).getMax(Direction.Axis.Y) == 1;
        boolean isSlab = abs(playerPos.getY() - (int) playerPos.getY()) - 0.5 == 0;
        if (isSlab && isNextBlock) {
            //LOG.info("Found block");
        }
    }
}
