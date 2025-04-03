package com.jalvaviel.addon.ChunkTrailer.FlightStats;

import com.jalvaviel.addon.ChunkTrailer.FlightData.FlightData;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.jalvaviel.addon.utils.WaypointUtils.*;
import static meteordevelopment.meteorclient.utils.player.ChatUtils.info;

public class FlightStatsManager {

    public static void showStats(String filename, FlightStats flightStats) { // Stats for the whole flight, saved on the file header.
        info("(highlight)-- FLIGHT FILE STATS --");
        info("(highlight)Filename: " + filename);
        info("(highlight)Total Waypoints: " + String.format("%d", flightStats.totalWaypoints()));
        info("(highlight)Absolute distance: " + String.format("%.3f blocks.", flightStats.totalDistance()));
        info("(highlight)Cumulative distance: " + String.format("%.3f blocks.", flightStats.cumulativeDistance()));
        info("(highlight)Time elapsed: " + String.format("%s", flightStats.duration()));
        info("");
    }

    public static void showCurrentStats(LocalTime startTime, int firstWaypointIndex, int currentWaypointIndex, FlightData flightData) { // Stats for the current flight replay being flown.
        info("(highlight)-- CURRENT FLIGHT STATS --");
        info("(highlight)Waypoints flown: " + String.format("%d", currentWaypointIndex-firstWaypointIndex));
        info("(highlight)Absolute distance flown: " +
            String.format("%.3f blocks.", getAbsoluteDistance(firstWaypointIndex,currentWaypointIndex,flightData)));
        info("(highlight)Cumulative distance flown: " +
            String.format("%.3f blocks.", getCumulativeDistance(firstWaypointIndex,currentWaypointIndex,flightData)));
        info("(highlight)Time elapsed: " + String.format("%s", LocalTime.MIDNIGHT
            .plus(Duration.between(startTime,LocalTime.now()))
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        info("(highlight)Estimated Completion: " +
            String.format("%.3f", getCompletion(currentWaypointIndex,flightData)) + " percent.");
        info("");
    }
}
