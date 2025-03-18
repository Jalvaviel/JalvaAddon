package com.jalvaviel.addon.ChunkTrailer;

import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;

public record FlightStats(int version, String world, Dimension dimension, ReplayMode mode, String timestamp, String duration,
                          int totalWaypoints, double totalDistance, double cumulativeDistance) {
    public static final short REPLAY_VERSION = 2;
    public static FlightStats genDummyMetadata(ReplayMode mode) {
        return new FlightStats(REPLAY_VERSION, Utils.getWorldName().replaceAll("[<>:\"/\\\\|?*]", "_"),
            PlayerUtils.getDimension(), mode, "01-01-2025-00-00-00", "00:00:00",
            0,0,0);
    }
}
