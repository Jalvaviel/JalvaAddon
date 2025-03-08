package com.jalvaviel.addon.modules;


import com.google.gson.JsonSyntaxException;
import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.ChunkTrailer.*;
import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.jalvaviel.addon.ChunkTrailer.FlightMetadata.REPLAY_VERSION;
import static com.jalvaviel.addon.ChunkTrailer.WaypointUtils.*;
import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT;

public class ChunkTrailer extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");


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
        .supplier(ReplayFileManager::getReplayFiles)
        .defaultValue(EMPTY_REPLAY_FOLDER_STRING)
        .build()
    );

    private final Setting<LoadMode> loadMode = sgGeneral.add(new EnumSetting.Builder<LoadMode>()
        .name("load-mode")
        .description("Selects which checkpoint to start from.")
        .visible(() -> replayMode.get() == ReplayMode.Load)
        .defaultValue(LoadMode.All)
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

    private final Setting<Boolean> autoEnableElytraExtras = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-enable-elytra-extras")
        .description("Enables the ElytraExtras module automatically.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnDamage = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-on-damage")
        .description("Disables this module when receiving damage.")
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

    private FlightData currentFlightData;
    private String flightFilename;
    private float originalAngle;
    private Vec3d currentWaypoint;
    private int currentWaypointIndex;
    private int firstWaypointIndex;
    private LocalTime startTime;
    boolean exception = false;
    public static final String EMPTY_REPLAY_FOLDER_STRING = "No replays found.";

    private void instantiateFlightData() {
        startTime = LocalTime.now();
        switch (replayMode.get()) {
            case ReplayMode.Generate:
                originalAngle = mc.player.getYaw();
                currentFlightData = new FlightData(FlightMetadata.genDummyMetadata(ReplayMode.Generate), new ArrayList<>());
                currentFlightData.addWaypoint(new Vec3d(mc.player.getX(), NULL_Y_VALUE, mc.player.getZ())); //generateWaypoint(searchAngle.get(),originalAngle,1,1);
                currentWaypointIndex = 0;
                currentWaypoint = currentFlightData.getWaypoints().get(currentWaypointIndex);
                // TODO test if it can generate the next waypoint onTick instead of here
                break;
            case Save:
                currentFlightData = new FlightData(FlightMetadata.genDummyMetadata(ReplayMode.Save), new ArrayList<>());
                break;
            case Load:
                handleFileLoad();
                break;
        }
    }

    private void handleFileLoad() {
        try {
            exception = false;
            currentFlightData = ReplayFileManager.loadReplay(replay.get(), rewind.get());
        } catch (JsonSyntaxException jsonSyntaxException){
            warning("This replay might be using an older format, trying to convert...");
            try {
                currentFlightData = ReplayFileManager.convertOldReplay(replay.get());
                warning("Replay converted successfully.");
            } catch (Exception ex) {
                error("Couldn't convert the replay, maybe it's corrupted. (highlight)Stopping.");
                exception = true;
            }
        } catch (FileNotFoundException fileNotFoundException) {
            warning("Empty replay folder. (highlight)Stopping.");
            exception = true;
        } catch (Exception e) {
            error("Couldn't load the replay, maybe it's corrupted or has the wrong permissions. (highlight)Stopping.");
            exception = true;
        } finally {
            if (!exception) {
                if (loadMode.get() == LoadMode.Nearest)
                    currentWaypointIndex = WaypointUtils.getNearestWaypoint(currentFlightData.getWaypoints());
                else currentWaypointIndex = 0;
                firstWaypointIndex = currentWaypointIndex;
                currentWaypoint = currentFlightData.getWaypoints().get(currentWaypointIndex);
            } else toggle();
        }
    }

    private void handleFileSave() {
        try {
            String worldName = Utils.getWorldName().replaceAll("[<>:\"/\\\\|?*]", "_");
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = worldName + "(" + date + ")";
            currentFlightData.updateMetadata(startTime, date);
            //if (currentFlightData.getFlightMetadata().mode() == ReplayMode.Generate) currentFlightData.getWaypoints().removeLast();
            ReplayFileManager.saveReplay(currentFlightData, filename);
            flightFilename = filename;
        } catch (IndexOutOfBoundsException e) {
            if (e.getMessage().contains("Index 0 out of bounds for length 0"))
                warning("Couldn't save the replay, since there aren't any waypoints. (highlight)Stopping.");
        } catch (Exception e) {
            error("Couldn't save the replay, maybe the directory hasn't got enough permissions. (highlight)Stopping.");
        }
    }

    @Override
    public void onActivate() {
        assert mc.player != null;
        instantiateFlightData();
        if (!autoEnableElytraExtras.get()) warning("You don't have ElytraExtras enabled, consider enabling it.");
        if (!Modules.get().isActive(ElytraExtras.class) && autoEnableElytraExtras.get()) {
            Modules.get().get(ElytraExtras.class).fixYaw.set(false);
            Modules.get().get(ElytraExtras.class).toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (exception) return;
        try {
            if (replayMode.get() != ReplayMode.Load) handleFileSave();
            if (flightStats.get()) {
                if (replayMode.get() != ReplayMode.Load) {
                    FlightStatsManager.showMetadata(flightFilename, currentFlightData.getFlightMetadata());
                } else {
                    FlightStatsManager.showCurrentStats(startTime, firstWaypointIndex, currentWaypointIndex, currentFlightData);
                }
            }
            if (Modules.get().isActive(ElytraExtras.class) && autoEnableElytraExtras.get())
                Modules.get().get(ElytraExtras.class).toggle();
        } catch (Exception ignored) {}
    }


    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release) return;
        if (event.key == saveWaypoint.get().getValue() && replayMode.get() == ReplayMode.Save) currentFlightData.addWaypoint(mc.player.getPos());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release) return;
        if (event.button == saveWaypoint.get().getValue() && replayMode.get() == ReplayMode.Save) currentFlightData.addWaypoint(mc.player.getPos());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (replayMode.get() == ReplayMode.Save) return;
        Vec3d playerPos = mc.player.getPos();
        double distanceToWaypoint = (currentFlightData.getFlightMetadata().mode() == ReplayMode.Save) ? currentWaypoint.distanceTo(playerPos) : getHorizontalDistance(currentWaypoint,playerPos);
        if (distanceToWaypoint < deltaDistance.get()) {
            if (replayMode.get() == ReplayMode.Generate) {
                if (angleOverlap.get()) currentFlightData.generateWaypoint(searchAngle.get(),originalAngle,minDistance.get(),maxDistance.get());
                else currentFlightData.generateWaypoint(searchAngle.get(),mc.player.getYaw(),minDistance.get(),maxDistance.get());
            }
            if (replayMode.get() == ReplayMode.Load && currentWaypointIndex+1 >= currentFlightData.getWaypoints().size()) { // currentFlightData.getFlightMetadata().totalWaypoints() Failsafe in case the file's totalWaypoints is not the same as the real amount.
                toggle();
                return;
            }
            currentWaypointIndex++;
            currentWaypoint = currentFlightData.getWaypoints().get(currentWaypointIndex);
        }
        lookAtWaypoint(currentWaypoint);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (renderWaypoints.get()) {
            if (occlusionCulling.get()) {
                event.renderer.triangles.depthTest = true;
                event.renderer.lines.depthTest = true;
            }
            for (int waypoint = 0; waypoint < currentFlightData.getWaypoints().size(); waypoint++) {
                SettingColor color = (waypoint == currentWaypointIndex) ? sideColorBox.get() : prevSideColorBox.get();
                event.renderer.box(currentFlightData.getWaypoints().get(waypoint).x - 0.25, mc.world.getBottomY(), currentFlightData.getWaypoints().get(waypoint).z - 0.25,
                    currentFlightData.getWaypoints().get(waypoint).x + 0.25, mc.world.getTopY(), currentFlightData.getWaypoints().get(waypoint).z + 0.25, color, color, ShapeMode.Sides, 0);
                event.renderer.box(currentFlightData.getWaypoints().get(waypoint).x - deltaDistance.get(), currentFlightData.getWaypoints().get(waypoint).y - deltaDistance.get(), currentFlightData.getWaypoints().get(waypoint).z - deltaDistance.get(),
                    currentFlightData.getWaypoints().get(waypoint).x + deltaDistance.get(), currentFlightData.getWaypoints().get(waypoint).y + deltaDistance.get(), currentFlightData.getWaypoints().get(waypoint).z + deltaDistance.get(), color, color, ShapeMode.Sides, 0);
            }
        }
    }

    @EventHandler(
        priority = -200
    )
    private void onGameDisconnected(GameLeftEvent event) {
        info("Disconnecting and saving file...");
        toggle();
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        if (this.disableOnDamage.get()) {
            this.toggle();
        }
    }
}

