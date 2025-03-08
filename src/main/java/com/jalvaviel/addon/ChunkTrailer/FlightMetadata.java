package com.jalvaviel.addon.ChunkTrailer;

import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.util.math.Vec3d;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.jalvaviel.addon.ChunkTrailer.WaypointUtils.getCumulativeDistance;
import static com.jalvaviel.addon.ChunkTrailer.WaypointUtils.getHorizontalDistance;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public record FlightMetadata(int version, Dimension dimension, ReplayMode mode, String timestamp, String duration,
                             int totalWaypoints, double totalDistance, double cumulativeDistance) {
    public static final short REPLAY_VERSION = 2;
    public static FlightMetadata genDummyMetadata(ReplayMode mode) {
        return new FlightMetadata(REPLAY_VERSION, PlayerUtils.getDimension(),mode,
            "01-01-2025-00-00-00", "00:00:00", 0,0,0);
    }
}
