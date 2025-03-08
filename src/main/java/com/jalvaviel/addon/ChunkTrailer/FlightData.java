package com.jalvaviel.addon.ChunkTrailer;

import net.minecraft.util.math.Vec3d;

import java.time.Duration;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.jalvaviel.addon.ChunkTrailer.FlightMetadata.REPLAY_VERSION;
import static com.jalvaviel.addon.utils.WaypointUtils.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class FlightData {
    private FlightMetadata flightMetadata;
    private ArrayList<Vec3d> waypoints;

    public FlightData(FlightMetadata flightMetadata, ArrayList<Vec3d> waypoints) {
        this.flightMetadata = flightMetadata;
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

    public FlightMetadata getFlightMetadata() {
        return flightMetadata;
    }

    public void setFlightMetadata(FlightMetadata flightMetadata) {
        this.flightMetadata = flightMetadata;
    }

    public void updateMetadata(LocalTime startTime, String date) {
        String duration = LocalTime.MIDNIGHT.plus(Duration.between(startTime,LocalTime.now())).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double absoluteDistance = getAbsoluteDistance(0,this);
        double cumulativeDistance = getCumulativeDistance(0, getWaypoints().size()-1, this);
        setFlightMetadata(new FlightMetadata(REPLAY_VERSION, getFlightMetadata().dimension(),
            getFlightMetadata().mode(), date, duration, getWaypoints().size(), absoluteDistance, cumulativeDistance));
    }
}

