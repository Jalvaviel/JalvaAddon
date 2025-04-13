package com.jalvaviel.addon.utils;

import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.item.ItemStack;
import org.joml.Vector3d;

public class ColorUtils {
    public static Color getInterpolatedColor(Color startColor, Color endColor, ItemStack itemStack) {
        int position = itemStack.getCount();
        int maxPositions = itemStack.getMaxCount();
        position = Math.max(0, Math.min(position, maxPositions - 1));

        float ratio = (float) position / (maxPositions - 1);

        float startR = startColor.r / 255f;
        float startG = startColor.g / 255f;
        float startB = startColor.b / 255f;
        float startA = startColor.a / 255f;

        float endR = endColor.r / 255f;
        float endG = endColor.g / 255f;
        float endB = endColor.b / 255f;
        float endA = endColor.a / 255f;

        float r = (1 - ratio) * startR + ratio * endR;
        float g = (1 - ratio) * startG + ratio * endG;
        float b = (1 - ratio) * startB + ratio * endB;
        float a = (1 - ratio) * startA + ratio * endA;

        return new Color(r, g, b, a);
    }

    public static boolean checkCorner(double x, double y, double z, Vector3d min, Vector3d max) {
        Vector3d pos = new Vector3d(x,y,z);
        if (!NametagUtils.to2D(pos, 1.0F)) {
            return true;
        } else {
            if (pos.x < min.x) {
                min.x = pos.x;
            }

            if (pos.y < min.y) {
                min.y = pos.y;
            }

            if (pos.z < min.z) {
                min.z = pos.z;
            }

            if (pos.x > max.x) {
                max.x = pos.x;
            }

            if (pos.y > max.y) {
                max.y = pos.y;
            }

            if (pos.z > max.z) {
                max.z = pos.z;
            }

            return false;
        }
    }
}
