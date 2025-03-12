package com.jalvaviel.addon.ChunkTrailer;

import net.minecraft.util.math.Vec3d;

import java.time.Duration;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.jalvaviel.addon.ChunkTrailer.FlightStats.REPLAY_VERSION;
import static com.jalvaviel.addon.utils.WaypointUtils.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class FlightData implements IFlightData{
    private FlightStats flightStats;
    private ArrayList<Vec3d> waypoints;

    public FlightData(FlightStats flightStats, ArrayList<Vec3d> waypoints) {
        this.flightStats = flightStats;
        this.waypoints = waypoints;
    }

    public ArrayList<Vec3d> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(Vec3d waypoint) { waypoints.add(waypoint); }

    public void generateWaypoint(int searchAngle, float originalAngle, int minDistance, int maxDistance) {
        float yaw = ThreadLocalRandom.current().nextFloat(originalAngle-searchAngle, originalAngle+searchAngle);
        float distance = (minDistance == maxDistance) ? minDistance :
            ThreadLocalRandom.current().nextFloat(Math.min(minDistance, maxDistance), Math.max(minDistance, maxDistance));
        double dx = -sin(Math.toRadians(yaw)) * distance;
        double dz = cos(Math.toRadians(yaw)) * distance;
        waypoints.add(new Vec3d(waypoints.getLast().x + dx, NULL_Y_VALUE, waypoints.getLast().z + dz));
    }

    public void setWaypoints(ArrayList<Vec3d> waypoints) {
        this.waypoints = waypoints;
    }

    public FlightStats getFlightStats() {
        return flightStats;
    }

    public void setFlightStats(FlightStats flightStats) {
        this.flightStats = flightStats;
    }

    public void updateStats(LocalTime startTime, String date) {
        String duration = LocalTime.MIDNIGHT.plus(Duration.between(startTime,LocalTime.now())).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double absoluteDistance = getAbsoluteDistance(0,getWaypoints().size()-1, this);
        double cumulativeDistance = getCumulativeDistance(0, getWaypoints().size()-1, this);
        setFlightStats(new FlightStats(REPLAY_VERSION, getFlightStats().dimension(),
            getFlightStats().mode(), date, duration, getWaypoints().size(), absoluteDistance, cumulativeDistance));
    }
}

