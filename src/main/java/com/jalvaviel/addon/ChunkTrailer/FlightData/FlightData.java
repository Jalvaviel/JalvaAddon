package com.jalvaviel.addon.ChunkTrailer.FlightData;

import com.jalvaviel.addon.ChunkTrailer.FlightStats.FlightStats;
import net.minecraft.util.math.Vec3d;

import java.time.Duration;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.jalvaviel.addon.ChunkTrailer.FlightStats.FlightStats.REPLAY_VERSION;
import static com.jalvaviel.addon.utils.WaypointUtils.*;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FlightData implements IFlightData {
    private FlightStats flightStats;
    private ArrayList<Vec3d> waypoints;

    public FlightData(FlightStats flightStats, ArrayList<Vec3d> waypoints) {
        this.flightStats = flightStats;
        this.waypoints = waypoints;
    }

    public int getNearestWaypointIndex(int maxDistance) {
        if (waypoints.isEmpty() || waypoints.size() == 1) return 0;
        double minDistance = getDistance(waypoints.getFirst(),mc.player.getPos(),getFlightStats().mode());
        int nearestIndex = 0;
        for (int index = 1; index < waypoints.size(); index++) {
            double distance = getDistance(waypoints.get(index),mc.player.getPos(),getFlightStats().mode()); //waypoints.get(index).distanceTo(mc.player.getPos());
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = index;
            }
        }
        return (getDistance(waypoints.get(nearestIndex),mc.player.getPos(), getFlightStats().mode()) < maxDistance) ? nearestIndex : -1;
    }

    public int getNearestWaypointIndex() {
        return getNearestWaypointIndex(Integer.MAX_VALUE);
    }

    public Vec3d getNearestWaypoint() {return waypoints.get(getNearestWaypointIndex()); }

    public ArrayList<Vec3d> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(Vec3d waypoint) { waypoints.add(waypoint); }

    public void addWaypoint(Vec3d waypoint, int position) { waypoints.add(position,waypoint); }

    public void addWaypointWithEditMode(Vec3d waypoint) {
        int nearestIndex = getNearestWaypointIndex();
        Vec3d A = waypoints.get(nearestIndex);
        Vec3d B = nearestIndex > 0 ? waypoints.get(nearestIndex - 1) : null;
        Vec3d C = nearestIndex < waypoints.size() - 1 ? waypoints.get(nearestIndex + 1) : null;
        Vec3d AP = mc.player.getPos().subtract(A);
        Vec3d AN;
        Vec3d ABnorm;
        Vec3d ACnorm;
        if (B != null && C != null) {
            ABnorm = B.subtract(A).normalize();
            ACnorm = C.subtract(A).normalize();
            AN = ABnorm.subtract(ACnorm);
        } else if (B != null) {
            ABnorm = B.subtract(A).normalize();
            AN = ABnorm;
        } else {
            ACnorm = C.subtract(A).normalize();
            AN = ACnorm.negate();
        }

        double dotProduct = AP.dotProduct(AN);
        if (dotProduct > 0) addWaypoint(waypoint, nearestIndex);
        else addWaypoint(waypoint, nearestIndex+1);
    }

    public void removeWaypoint(Vec3d waypoint) { waypoints.remove(waypoint); }
    public void removeWaypoint(int position) { waypoints.remove(position); }

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
        setFlightStats(new FlightStats(REPLAY_VERSION, getFlightStats().world(), getFlightStats().dimension(),
            getFlightStats().mode(), date, duration, getWaypoints().size(), absoluteDistance, cumulativeDistance));
    }
}

