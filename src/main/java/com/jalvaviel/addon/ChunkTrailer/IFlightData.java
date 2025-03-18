package com.jalvaviel.addon.ChunkTrailer;

import net.minecraft.util.math.Vec3d;

import java.time.LocalTime;
import java.util.ArrayList;

public interface IFlightData {
    ArrayList<Vec3d> getWaypoints();
    void addWaypoint(Vec3d waypoint);
    public void removeWaypoint(Vec3d waypoint);
    void generateWaypoint(int searchAngle, float originalAngle, int minDistance, int maxDistance);
    void setWaypoints(ArrayList<Vec3d> waypoints);
    FlightStats getFlightStats();
    void setFlightStats(FlightStats flightStats);
    void updateStats(LocalTime startTime, String date);
}
