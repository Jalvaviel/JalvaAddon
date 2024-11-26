package com.jalvaviel.addon.modules;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jalvaviel.addon.Addon;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class ChunkTrailer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private enum ReplayMode {
        Generate,
        Save,
        Load
    }

    public ChunkTrailer() {
        super(Addon.CATEGORY, "chunk-trailer", "Generates random chunk trails");
    }

    private final Setting<ReplayMode> replayMode = sgGeneral.add(new EnumSetting.Builder<ReplayMode>()
        .name("replay-mode")
        .description("Mode of replay.")
        .defaultValue(ReplayMode.Generate)
        .build()
    );

    private final Setting<Integer> deltaDistance = sgGeneral.add(new IntSetting.Builder()
        .name("delta-distance")
        .description("The distance threshold of a waypoint.")
        .defaultValue(3)
        .visible(() -> replayMode.get() != ReplayMode.Save)
        .sliderRange(1,20)
        .build()
    );

    private final Setting<String> replay = sgGeneral.add(new ProvidedStringSetting.Builder()
        .name("replay")
        .description("Select a replay file.")
        .visible(() -> replayMode.get() == ReplayMode.Load)
        .supplier(ChunkTrailer::getReplayFiles)
        .defaultValue("")
        .build()
    );

    private final Setting<Boolean> rewind = sgGeneral.add(new BoolSetting.Builder()
        .name("rewind")
        .description("Does the trail backwards.")
        .defaultValue(false)
        .visible(() -> replayMode.get() == ReplayMode.Load)
        .build()
    );

    private final Setting<Integer> searchAngle = sgGeneral.add(new IntSetting.Builder()
        .name("search-angle")
        .description("The angle deviation from the player's current yaw.")
        .defaultValue(20)
        .sliderRange(1,180)
        .visible(() -> replayMode.get() == ReplayMode.Generate)
        .build()
    );

    private final Setting<Integer> minDistance = sgGeneral.add(new IntSetting.Builder()
        .name("min-distance")
        .description("The minimum block distance to move the player before checking another spot.")
        .defaultValue(500)
        .sliderRange(10, 10000)
        .visible(() -> replayMode.get() == ReplayMode.Generate)
        .build()
    );

    private final Setting<Integer> maxDistance = sgGeneral.add(new IntSetting.Builder()
        .name("max-distance")
        .description("The maximum block distance to move the player before checking another spot.")
        .defaultValue(500)
        .sliderRange(10, 10000)
        .visible(() -> replayMode.get() == ReplayMode.Generate)
        .build()
    );

    private final Setting<Boolean> angleOverlap = sgGeneral.add(new BoolSetting.Builder()
        .name("angle-overlap")
        .description("Generates the new facing angles from the original angle instead of the last one.")
        .defaultValue(true)
        .visible(() -> replayMode.get() == ReplayMode.Generate)
        .build()
    );

    private final Setting<Keybind> saveWaypoint = sgGeneral.add(new KeybindSetting.Builder()
        .name("save-waypoint-keybind")
        .description("The keybind to save a waypoint.")
        .defaultValue(Keybind.fromKey(GLFW_KEY_GRAVE_ACCENT))
        .visible(() -> replayMode.get() == ReplayMode.Save)
        .build()
    );


    private final Setting<Boolean> autoEnableElytraBoost = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-enable-elytra-boost")
        .description("Enables the ElytraBoostPlus module automatically.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> flightStats = sgGeneral.add(new BoolSetting.Builder()
        .name("flight-stats")
        .description("Shows flight stats when done flying.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> renderWaypoints = sgRender.add(new BoolSetting.Builder()
        .name("render-waypoints")
        .description("Renders the waypoints when flying.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> sideColorBox = sgRender.add(new ColorSetting.Builder()
        .name("next-side-color")
        .description("The side color of the next waypoint.")
        .defaultValue(new SettingColor(16,144,106, 100))
        .visible(renderWaypoints::get)
        .build()
    );

    private final Setting<SettingColor> prevSideColorBox = sgRender.add(new ColorSetting.Builder()
        .name("previous-line-color")
        .description("The side color of the previous waypoints.")
        .defaultValue(new SettingColor(144,106,16, 100))
        .visible(renderWaypoints::get)
        .build()
    );

    private final Setting<Boolean> occlusionCulling = sgRender.add(new BoolSetting.Builder()
        .name("occlusion-culling")
        .description("Doesn't render the beams behind blocks.")
        .defaultValue(false)
        .visible(renderWaypoints::get)
        .build()
    );

    ArrayList<Waypoint> waypoints = new ArrayList<>();
    int nextWaypointNumber;
    long startEpochTime;
    float originalAngle;

    private static String[] getReplayFiles() {
        try {
            Path replaysPath = FabricLoader.getInstance().getGameDir().resolve("meteor-client/trail-replays");
            if (Files.notExists(replaysPath)) return new String[0];
            return Files.list(replaysPath)
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .map(path -> path.getFileName().toString())
                .toArray(String[]::new);
        } catch (IOException e) {
            LogUtils.getLogger().error("Couldn't retrieve replay files.", e);
        }
        return new String[0];
    }

    private float generateNextYaw() {
        assert mc.player != null;
        return angleOverlap.get() ? ThreadLocalRandom.current().nextFloat(originalAngle-searchAngle.get(), originalAngle+searchAngle.get())
        : ThreadLocalRandom.current().nextFloat(mc.player.getYaw()-searchAngle.get(), mc.player.getYaw()+searchAngle.get());
    }

    private float generateNextDistance() {
        assert mc.player != null;
        if (Objects.equals(maxDistance.get(), minDistance.get())) return minDistance.get();
        return ThreadLocalRandom.current().nextFloat(Math.min(minDistance.get(), maxDistance.get()), Math.max(minDistance.get(), maxDistance.get()));
    }

    private void saveReplay() throws IOException {
        assert mc.world != null;
        Path replaysPath = FabricLoader.getInstance().getGameDir().resolve("meteor-client/trail-replays");
        if (Files.notExists(replaysPath)) Files.createDirectory(replaysPath);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String isGenerated = replayMode.get() == ReplayMode.Generate ? "G" : "S";

        FileWriter writer = new FileWriter(replaysPath + "/"
            + Utils.getWorldName() + "(" + (int) waypoints.getLast().x + "," + (int) waypoints.getLast().z + ")" + isGenerated + ".json");
        gson.toJson(waypoints, writer);
        writer.close();
    }

    private void loadReplay(String filename) throws IOException {
        if (!filename.contains(".json")) return;
        waypoints.clear();
        Path replaysPath = FabricLoader.getInstance().getGameDir().resolve("meteor-client/trail-replays");
        if (Files.notExists(replaysPath)) Files.createDirectory(replaysPath);
        Gson gson = new Gson();
        FileReader reader = new FileReader(replaysPath.resolve(filename).toFile());
        Waypoint[] waypointList = gson.fromJson(reader, Waypoint[].class);
        for (Waypoint waypoint : waypointList) {
            if (rewind.get()) waypoints.addFirst(new Waypoint(waypoint.x, waypoint.y, waypoint.z));
            else waypoints.add(new Waypoint(waypoint.x, waypoint.y, waypoint.z));
        }
        reader.close();
        nextWaypointNumber = 0;
    }

    private void showStats() {
        assert mc.player != null;
        if (replayMode.get() == ReplayMode.Load && !replay.get().contains(".json") ) { return;}
        long timeElapsed = (System.currentTimeMillis() / 1000) - startEpochTime;
        long hours = TimeUnit.SECONDS.toHours(timeElapsed);
        long minutes = TimeUnit.SECONDS.toMinutes(timeElapsed) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(timeElapsed));
        long seconds = timeElapsed - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeElapsed));
        double distance = getCumulativeDistance();

        info("(highlight)Total Waypoints: "+"%d",waypoints.size());
        info("(highlight)Absolute distance: "+"%d blocks.",nextWaypointNumber > 0 ? (int) waypoints.getFirst().horizontalDistanceTo(mc.player.getPos()) : 0);
        info("(highlight)Cumulative distance: "+"%d blocks.",(int) distance);
        info("(highlight)Time elapsed: "+"%dh %dm %ds",hours,minutes,seconds);
    }

    private double getCumulativeDistance() {
        double distance = 0;
        assert mc.player != null;
        if (nextWaypointNumber > 1) {
            for (int i = 0; i < nextWaypointNumber - 1; i++) {
                Waypoint waypoint1 = waypoints.get(i);
                Waypoint waypoint2 = waypoints.get(i + 1);
                distance += waypoint1.horizontalDistanceTo(new Vec3d(waypoint2.x, 0, waypoint2.z));
            }
            distance += waypoints.get(nextWaypointNumber-1).horizontalDistanceTo(mc.player.getPos());
        }
        if (nextWaypointNumber == 1) {
            distance += waypoints.getFirst().horizontalDistanceTo(mc.player.getPos());
        }
        return distance;
    }

    @Override
    public void onActivate() {
        assert mc.player != null;
        nextWaypointNumber = 0;
        startEpochTime = System.currentTimeMillis() / 1000;
        originalAngle = mc.player.getYaw();
        if (!Modules.get().isActive(ElytraBoostPlus.class)) {
            if (autoEnableElytraBoost.get()) {
                Modules.get().get(ElytraBoostPlus.class).toggle();
            } else {
                warning("You don't have ElytraBoostPlus enabled, consider enabling it.");
            }
        }

        if (replayMode.get() == ReplayMode.Generate){
            Waypoint origin = new Waypoint(mc.player.getX(), -69420, mc.player.getZ());
            waypoints.add(origin);
            waypoints.add(origin.generateNextWaypoint(generateNextYaw(), generateNextDistance()));
            nextWaypointNumber = waypoints.size()-1;
        }
        if (replayMode.get() == ReplayMode.Load){
            try { loadReplay(replay.get()); } catch (IOException ignored) {}
        }
        if (replayMode.get() == ReplayMode.Save){
            Waypoint origin = new Waypoint(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            waypoints.add(origin);
            nextWaypointNumber = waypoints.size()-1;
        }
    }

    @Override
    public void onDeactivate() {
        assert mc.player != null;
        if (flightStats.get()) {
            showStats();
        }
        if (replayMode.get() != ReplayMode.Load) {
            try {
                saveReplay();
            } catch (IOException e) {
                error("Unable to save replay.");
            }
        }
        if (autoEnableElytraBoost.get() && Modules.get().isActive(ElytraBoostPlus.class)) Modules.get().get(ElytraBoostPlus.class).toggle();
        waypoints.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        assert mc.player != null;
        if (event.action == KeyAction.Release) return;
        if (event.key == saveWaypoint.get().getValue()) waypoints.add(new Waypoint(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        assert mc.player != null;
        if (event.action == KeyAction.Release) return;
        if (event.button == saveWaypoint.get().getValue()) waypoints.add(new Waypoint(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        assert mc.player != null;
        if (replayMode.get() == ReplayMode.Save) return;
        if (replayMode.get() == ReplayMode.Load && nextWaypointNumber >= waypoints.size()) {
            toggle();
            return;
        }
        Waypoint objectiveWaypoint = waypoints.get(nextWaypointNumber);
        double distanceTo = objectiveWaypoint.y == -69420 ? objectiveWaypoint.horizontalDistanceTo(mc.player.getPos()) : objectiveWaypoint.getWaypoint().distanceTo(mc.player.getPos());
        if (distanceTo < deltaDistance.get()){
            if (replayMode.get() == ReplayMode.Generate) waypoints.add(objectiveWaypoint.generateNextWaypoint(generateNextYaw(), generateNextDistance()));
            nextWaypointNumber++;
        } else {
            objectiveWaypoint.lookAtWaypoint();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (renderWaypoints.get()) {
            assert mc.world != null;
            if (occlusionCulling.get()) {
                event.renderer.triangles.depthTest = true;
                event.renderer.lines.depthTest = true;
            }
            for (int waypoint = 0; waypoint < waypoints.size(); waypoint++) {
                boolean isNext = waypoint == nextWaypointNumber;
                if (!isNext) {
                    event.renderer.box(waypoints.get(waypoint).x - 0.25, mc.world.getBottomY(), waypoints.get(waypoint).z - 0.25,
                        waypoints.get(waypoint).x + 0.25, mc.world.getTopY(), waypoints.get(waypoint).z + 0.25, prevSideColorBox.get(), prevSideColorBox.get(), ShapeMode.Sides, 0);
                    event.renderer.box(waypoints.get(waypoint).x - deltaDistance.get(), waypoints.get(waypoint).y - deltaDistance.get(), waypoints.get(waypoint).z - deltaDistance.get(),
                        waypoints.get(waypoint).x + deltaDistance.get(), waypoints.get(waypoint).y + deltaDistance.get(), waypoints.get(waypoint).z + deltaDistance.get(), prevSideColorBox.get(), prevSideColorBox.get(), ShapeMode.Sides, 0);
                } else {
                    event.renderer.box(waypoints.get(waypoint).x - 0.25, mc.world.getBottomY(), waypoints.get(waypoint).z - 0.25,
                        waypoints.get(waypoint).x + 0.25, mc.world.getTopY(), waypoints.get(waypoint).z + 0.25, sideColorBox.get(), sideColorBox.get(), ShapeMode.Sides, 0);
                    event.renderer.box(waypoints.get(waypoint).x - deltaDistance.get(), waypoints.get(waypoint).y - deltaDistance.get(), waypoints.get(waypoint).z - deltaDistance.get(),
                        waypoints.get(waypoint).x + deltaDistance.get(), waypoints.get(waypoint).y + deltaDistance.get(), waypoints.get(waypoint).z + deltaDistance.get(), sideColorBox.get(), sideColorBox.get(), ShapeMode.Sides, 0);
                }
            }
        }
    }

    private class Waypoint {
        double x, y, z;

        protected Waypoint(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vec3d getWaypoint() { return new Vec3d(x, y, z); }

        protected void lookAtWaypoint() {
            assert mc.player != null;
            //mc.player.lookAt(mc.player.getCommandSource().getEntityAnchor(), getWaypoint());
            if (y == -69420) {
                Vec3d vec3d = mc.player.getPos();
                double d = this.x - vec3d.x;
                double f = this.z - vec3d.z;
                mc.player.setYaw(MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 57.2957763671875) - 90.0F));
                mc.player.prevYaw = mc.player.getYaw();
            } else {
                mc.player.lookAt(mc.player.getCommandSource().getEntityAnchor(), getWaypoint());
            }
        }

        protected Waypoint generateNextWaypoint(float yaw, double distance) {
            assert mc.player != null;
            double dx = -sin(Math.toRadians(yaw)) * distance;
            double dz = cos(Math.toRadians(yaw)) * distance;
            return new Waypoint(this.x + dx, -69420, this.z + dz);
        }

        protected double horizontalDistanceTo(Vec3d vec) {
            double d = vec.x - this.x;
            double f = vec.z - this.z;
            return Math.sqrt(d * d + f * f);
        }

        public String toString(){
            return "X: " + (int) this.x + ", Z: " + (int) this.z;
        }
    }
}

/*
@EventHandler
    private void onTick(TickEvent.Pre event) {
        assert mc.player != null;
        if (replayMode.get() == ReplayMode.Save) {
            Waypoint objectiveWaypoint = waypoints.getLast();
            if (objectiveWaypoint.distanceTo(mc.player.getPos()) < deltaDistance.get()){
                waypoints.add(objectiveWaypoint.generateNextWaypoint(generateNextYaw(), generateNextDistance()));
                nextWaypointNumber++;
            } else {
                objectiveWaypoint.lookAtWaypoint();
            }
        }
        if (replayMode.get() == ReplayMode.Load) {
            if (nextWaypointNumber >= waypoints.size()) {
                toggle();
                return;
            }
            Waypoint objectiveWaypoint = waypoints.get(nextWaypointNumber);
            if (objectiveWaypoint.distanceTo(mc.player.getPos()) < deltaDistance.get()){
                nextWaypointNumber++;
            } else {
                objectiveWaypoint.lookAtWaypoint();
            }
        }
    }
 */
