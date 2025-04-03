package com.jalvaviel.addon.utils;


import com.jalvaviel.addon.ChunkTrailer.FlightData.FlightData;
import com.jalvaviel.addon.ChunkTrailer.Enums.ReplayMode;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.stream.Stream;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WaypointUtils {
    public static final int NULL_Y_VALUE = -69420;
    public static void lookAtWaypoint(Vec3d waypoint) {
        if (waypoint.y == NULL_Y_VALUE) {
            Vec3d playerPos = mc.player.getPos();
            double d = waypoint.x - playerPos.x;
            double f = waypoint.z - playerPos.z;
            mc.player.setYaw(MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F));
            mc.player.setHeadYaw(mc.player.getYaw());
        } else {
            mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, waypoint);
        }
    }

    public static double getHorizontalDistance(Vec3d vec1, Vec3d vec2) {
        double d = vec2.x - vec1.x;
        double f = vec2.z - vec1.z;
        return Math.sqrt(d * d + f * f);
    }

    public static double getDistance(Vec3d vec1, Vec3d vec2, ReplayMode mode) {
        return (mode == ReplayMode.Generate) ? getHorizontalDistance(vec1, vec2) : vec1.distanceTo(vec2);
    }

    public static double getAbsoluteDistance(int firstWaypointIndex, int currentWaypointIndex, FlightData flightData) {
        if (currentWaypointIndex <= firstWaypointIndex) return 0;
        if (currentWaypointIndex != flightData.getWaypoints().size()-1) currentWaypointIndex-=1;
        Vec3d firstWaypoint = flightData.getWaypoints().get(firstWaypointIndex);
        Vec3d lastWaypoint = flightData.getWaypoints().get(currentWaypointIndex);
        return getDistance(firstWaypoint,lastWaypoint,flightData.getFlightStats().mode());
    }

    public static double getCumulativeDistance(int firstWaypointIndex, int currentWaypointIndex, FlightData flightData) {
        if (currentWaypointIndex <= firstWaypointIndex) return 0;
        ArrayList<Vec3d> waypoints = flightData.getWaypoints();
        double cumulativeDistance = 0;
        for (int i = firstWaypointIndex; i < currentWaypointIndex; i++) {
            cumulativeDistance += getDistance(waypoints.get(i),waypoints.get(i+1), flightData.getFlightStats().mode());
        }
        return cumulativeDistance;
    }

    public static double getCompletion(int currentWaypointIndex, FlightData flightData) {
        return (flightData.getWaypoints().size() > 1)
            ? ((currentWaypointIndex) * 100.0) / (flightData.getWaypoints().size() - 1)
            : 100.0;
    }

    public static double getYaw(Vec3d pos) {
        Vec3d playerPos = mc.player.getPos();
        double dx = pos.getX() - playerPos.getX();
        double dz = pos.getZ() - playerPos.getZ();
        double yawRadians = Math.atan2(-dx, dz);
        double yawDegrees = Math.toDegrees(yawRadians);
        return (yawDegrees + 360) % 360;
    }

    public static void generateSpiralBundle(FlightData flightData, int chunkDistance, Vec3d center, int quantity) {
        int blockDistance = chunkDistance * 16;
        flightData.addWaypoint(center);

        if (quantity <= 1) return;

        int layer = 1;
        int cornerCount = 0;

        while (cornerCount < quantity - 1) {
            for (Corner corner : Corner.values()) {
                // Calculate coordinates based on corner type
                Vec3d waypoint = switch (corner) {
                    case NE -> new Vec3d(
                        center.x + layer * blockDistance,
                        NULL_Y_VALUE,
                        center.z - layer * blockDistance
                    );
                    case SE -> new Vec3d(
                        center.x + layer * blockDistance,
                        NULL_Y_VALUE,
                        center.z + layer * blockDistance
                    );
                    case SW -> new Vec3d(
                        center.x - layer * blockDistance,
                        NULL_Y_VALUE,
                        center.z + layer * blockDistance
                    );
                    case NW -> new Vec3d(
                        center.x - layer * blockDistance,
                        NULL_Y_VALUE,
                        center.z - layer * blockDistance
                    );
                };

                flightData.addWaypoint(waypoint);
                cornerCount++;

                if (cornerCount >= quantity - 1) break;
            }

            layer++;
        }
    }

    private enum Corner {
        NE,  // North-East (top-right)
        SE,  // South-East (bottom-right)
        SW,  // South-West (bottom-left)
        NW   // North-West (top-left)
    }

    /*
    public static void generateSpiralBundle(FlightData flightData, int chunkDistance, Vec3d center, int quantity) {
        int blockDistance = chunkDistance * 16;
        flightData.addWaypoint(center);
        int layer = 1;
        int cornerCount = 0;
        while (cornerCount < quantity - 1) {
            Vec3d topRight = new Vec3d(
                center.x + layer * blockDistance,
                NULL_Y_VALUE,
                center.z - layer * blockDistance
            );
            flightData.addWaypoint(topRight);
            cornerCount++;
            if (cornerCount >= quantity - 1) break;

            Vec3d bottomRight = new Vec3d(
                center.x + layer * blockDistance,
                NULL_Y_VALUE,
                center.z + layer * blockDistance
            );
            flightData.addWaypoint(bottomRight);
            cornerCount++;
            if (cornerCount >= quantity - 1) break;

            Vec3d bottomLeft = new Vec3d(
                center.x - layer * blockDistance,
                NULL_Y_VALUE,
                center.z + layer * blockDistance
            );
            flightData.addWaypoint(bottomLeft);
            cornerCount++;
            if (cornerCount >= quantity - 1) break;

            Vec3d topLeft = new Vec3d(
                center.x - layer * blockDistance,
                NULL_Y_VALUE,
                center.z - layer * blockDistance
            );
            flightData.addWaypoint(topLeft);
            cornerCount++;
            if (cornerCount >= quantity - 1) break;

            layer++;
        }
    }
    */

    public static Vec3d getFromBlockPos(BlockPos blockPos) { return new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()); }
    public static BlockPos getFromVec3d(Vec3d vec3d) { return new BlockPos((int)vec3d.x,(int)vec3d.y,(int)vec3d.z); }
    public static Vec3d getFromChunkPos(ChunkPos chunkPos) { return new Vec3d(chunkPos.getCenterX(), NULL_Y_VALUE, chunkPos.getCenterZ()); }
    public static Vec3d getFromVector3d(Vector3d vector3d) { return new Vec3d(vector3d.x, vector3d.y, vector3d.z); }
}
