package com.jalvaviel.addon.utils;


import com.jalvaviel.addon.ChunkTrailer.FlightData;
import com.jalvaviel.addon.ChunkTrailer.ReplayMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WaypointUtils {
    public static final int NULL_Y_VALUE = -69420;
    public static void lookAtWaypoint(Vec3d waypoint) {
        if (waypoint.y == NULL_Y_VALUE) {
            Vec3d playerPos = mc.player.getPos();
            double d = waypoint.x - playerPos.x;
            double f = waypoint.z - playerPos.z;
            mc.player.setYaw(MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F));
            //mc.player.prevYaw = mc.player.getYaw();
            mc.player.setHeadYaw(mc.player.getYaw());
        } else {
            mc.player.lookAt(mc.player.getCommandSource().getEntityAnchor(), waypoint);
        }
    }

    public static double getHorizontalDistance(Vec3d vec1, Vec3d vec2) {
        double d = vec2.x - vec1.x;
        double f = vec2.z - vec1.z;
        return Math.sqrt(d * d + f * f);
    }

    public static int getNearestWaypoint(ArrayList<Vec3d> waypoints) {
        if (waypoints.isEmpty() || waypoints.size() == 1) return 0;
        double minDistance = waypoints.get(0).distanceTo(mc.player.getPos());
        int nearestIndex = 0;
        for (int index = 1; index < waypoints.size(); index++) {
            double distance = waypoints.get(index).distanceTo(mc.player.getPos());
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = index;
            }
        }
        return nearestIndex;
    }

    public static double getCumulativeDistance(int firstWaypointIndex, int currentWaypointIndex, FlightData flightData) {
        double cumulativeDistance = 0;
        if (currentWaypointIndex == firstWaypointIndex) return getAbsoluteDistance(firstWaypointIndex, flightData);
        ArrayList<Vec3d> waypoints = flightData.getWaypoints();
        for (int i = firstWaypointIndex; i < currentWaypointIndex-1; i++) {
            cumulativeDistance += (flightData.getFlightMetadata().mode() == ReplayMode.Save) ? waypoints.get(i).distanceTo(waypoints.get(i+1))
                : getHorizontalDistance(waypoints.get(i),waypoints.get(i+1));
        }
        cumulativeDistance += getAbsoluteDistance(currentWaypointIndex,flightData);
        return cumulativeDistance;
    }

    public static double getAbsoluteDistance(int firstWaypointIndex, FlightData flightData) {
        double absoluteDistance;
        if (flightData.getFlightMetadata().mode() == ReplayMode.Save) {
            absoluteDistance = mc.player.getPos().distanceTo(flightData.getWaypoints().get(firstWaypointIndex));
        } else {
            absoluteDistance = getHorizontalDistance(mc.player.getPos(), flightData.getWaypoints().get(firstWaypointIndex));
        }
        return absoluteDistance;
    }
}
