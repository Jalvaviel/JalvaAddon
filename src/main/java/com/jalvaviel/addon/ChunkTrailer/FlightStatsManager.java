package com.jalvaviel.addon.ChunkTrailer;

import net.minecraft.util.math.Vec3d;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.jalvaviel.addon.ChunkTrailer.WaypointUtils.*;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.player.ChatUtils.info;

public class FlightStatsManager {

    public static void showMetadata(String filename, FlightMetadata flightMetadata) { // Stats for the whole flight, saved on the file header.
        info("(highlight)-- REPLAY FILE STATS --");
        info("(highlight)Filename: " + filename);
        info("(highlight)Total Waypoints: " + String.format("%d", flightMetadata.totalWaypoints()));
        info("(highlight)Absolute distance: " + String.format("%.3f blocks.", flightMetadata.totalDistance()));
        info("(highlight)Cumulative distance: " + String.format("%.3f blocks.", flightMetadata.cumulativeDistance()));
        info("(highlight)Time elapsed: " + String.format("%s", flightMetadata.duration()));
        info("");
    }

    public static void showCurrentStats(LocalTime startTime, int firstWaypoint, int currentWaypoint, FlightData flightData) { // Stats for the current flight replay being flown.
        String duration = LocalTime.MIDNIGHT.plus(Duration.between(startTime,LocalTime.now())).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double absoluteDistance = getAbsoluteDistance(firstWaypoint, flightData);
        double cumulativeDistance = getCumulativeDistance(firstWaypoint, currentWaypoint, flightData);
        int totalWaypoints = flightData.getWaypoints().size();
        // FIXME Dirty approach since the last waypoint is the same when you're moving to it and as you've finished. Also double ternary.
        double completion = (totalWaypoints > 1)
            ? ((currentWaypoint - firstWaypoint) * 100.0) / (totalWaypoints - 1)
            : 100.0;
        info("(highlight)-- CURRENT REPLAY STATS --");
        info("(highlight)Waypoints flown: " + String.format("%d", currentWaypoint-firstWaypoint));
        info("(highlight)Absolute distance flown: " + String.format("%.3f blocks.", absoluteDistance));
        info("(highlight)Cumulative distance flown: " + String.format("%.3f blocks.", cumulativeDistance));
        info("(highlight)Time elapsed: " + String.format("%s", duration));
        info("(highlight)Completion: " + String.format("%.3f", completion) + " percent.");
        info("");
    }
}
