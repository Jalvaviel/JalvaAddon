package com.jalvaviel.addon.ChunkTrailer;

import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.Vec3d;

import static com.jalvaviel.addon.utils.WaypointUtils.NULL_Y_VALUE;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WaypointRenderer {

    private static final int lineHeightRender = (Modules.get().get(ElytraFly.class).flightMode.get() == ElytraFlightModes.Pitch40) ?
        (int) ((Modules.get().get(ElytraFly.class).pitch40lowerBounds.get()
            + Modules.get().get(ElytraFly.class).pitch40upperBounds.get()) / 2) : 250;

    public static void renderArrows(Renderer3D renderer, Vec3d first, Vec3d second, int amount, Color color) {
        if (first.y == NULL_Y_VALUE) {
            first = new Vec3d(first.x, lineHeightRender, first.z);
            second = new Vec3d(second.x, lineHeightRender, second.z);
        }
        Vec3d direction = second.subtract(first).normalize();
        Vec3d perpendicular1 = new Vec3d(-direction.z, 0, direction.x).normalize();
        Vec3d perpendicular2 = direction.crossProduct(perpendicular1).normalize();
        final double arrowSize = 0.75;
        for (int i = 0; i < amount + 1; i++) {
            Vec3d center = first.lerp(second, (double) i / (amount + 1));
            Vec3d tipVertex = center.add(direction.multiply(-arrowSize));
            Vec3d wingVertex1 = center.add(perpendicular1.multiply(arrowSize));
            Vec3d wingVertex2 = center.subtract(perpendicular1.multiply(arrowSize));
            Vec3d wingVertex3 = center.add(perpendicular2.multiply(arrowSize));
            Vec3d wingVertex4 = center.subtract(perpendicular2.multiply(arrowSize));
            renderer.triangles.triangle(
                renderer.triangles.vec3(tipVertex.x, tipVertex.y, tipVertex.z).color(color).next(),
                renderer.triangles.vec3(wingVertex1.x, wingVertex1.y, wingVertex1.z).color(color).next(),
                renderer.triangles.vec3(wingVertex2.x, wingVertex2.y, wingVertex2.z).color(color).next()
            );
            renderer.triangles.triangle(
                renderer.triangles.vec3(tipVertex.x, tipVertex.y, tipVertex.z).color(color).next(),
                renderer.triangles.vec3(wingVertex3.x, wingVertex3.y, wingVertex3.z).color(color).next(),
                renderer.triangles.vec3(wingVertex4.x, wingVertex4.y, wingVertex4.z).color(color).next()
            );
        }
    }

    public static void renderBeam(Renderer3D renderer, Vec3d waypoint, Color color) {
        final double beamSize = 0.25;
        renderer.box(waypoint.x - beamSize, mc.world.getBottomY(), waypoint.z - beamSize,
            waypoint.x + beamSize, mc.world.getTopY()*2, waypoint.z + beamSize, color, color,
            ShapeMode.Sides, 0);
    }

    public static void renderWaypoint(Renderer3D renderer, Vec3d waypoint, Color color) {
        final double waypointSize = 1;
        if (waypoint.y == NULL_Y_VALUE) waypoint = new Vec3d(waypoint.x,lineHeightRender,waypoint.z);
        renderer.box(waypoint.x - waypointSize, waypoint.y - waypointSize, waypoint.z - waypointSize,
            waypoint.x + waypointSize, waypoint.y + waypointSize, waypoint.z + waypointSize, color, color, ShapeMode.Sides, 0);
    }

    public static void renderLine(Renderer3D renderer, Vec3d first, Vec3d second, Color color) {
        if (first.y == NULL_Y_VALUE) renderer.line(first.x,lineHeightRender,first.z,second.x,lineHeightRender,second.z,color);
        else renderer.line(first.x,first.y,first.z,second.x,second.y,second.z,color);
    }
}
