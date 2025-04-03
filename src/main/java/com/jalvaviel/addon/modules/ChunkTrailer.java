package com.jalvaviel.addon.modules;


import com.google.gson.JsonSyntaxException;
import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.ChunkTrailer.Enums.GenerateMode;
import com.jalvaviel.addon.ChunkTrailer.Enums.LoadMode;
import com.jalvaviel.addon.ChunkTrailer.Enums.ReplayMode;
import com.jalvaviel.addon.ChunkTrailer.FileManager.ReplayFileManager;
import com.jalvaviel.addon.ChunkTrailer.FlightData.FlightData;
import com.jalvaviel.addon.ChunkTrailer.FlightStats.FlightStats;
import com.jalvaviel.addon.ChunkTrailer.FlightStats.FlightStatsManager;
import com.jalvaviel.addon.ChunkTrailer.Render.WaypointRenderer;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.jalvaviel.addon.utils.WaypointUtils.*;
import static org.lwjgl.glfw.GLFW.*;

public class ChunkTrailer extends Module{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMode = settings.createGroup("Mode");
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

    private final Setting<Integer> deltaDistance = sgMode.add(new IntSetting.Builder()
        .name("delta-distance")
        .description("The distance threshold of a waypoint.")
        .defaultValue(3)
        .visible(() -> replayMode.get() == ReplayMode.Load)
        .sliderRange(1,20)
        .min(1)
        .build()
    );

    private final Setting<String> replay = sgMode.add(new ProvidedStringSetting.Builder()
        .name("replay")
        .description("Select a replay file.")
        .visible(() -> replayMode.get() == ReplayMode.Load || replayMode.get() == ReplayMode.Edit)
        .supplier(ReplayFileManager::getReplayFiles)
        .defaultValue(SELECT_REPLAY_STRING)
        .build()
    );

    private final Setting<LoadMode> loadMode = sgMode.add(new EnumSetting.Builder<LoadMode>()
        .name("load-mode")
        .description("Selects which checkpoint to start from.")
        .visible(() -> replayMode.get() == ReplayMode.Load)
        .defaultValue(LoadMode.All)
        .build()
    );

    private final Setting<Boolean> rewind = sgMode.add(new BoolSetting.Builder()
        .name("rewind")
        .description("Does the trail backwards.")
        .defaultValue(false)
        .visible(() -> replayMode.get() == ReplayMode.Load)
        .build()
    );

    private final Setting<GenerateMode> generateMode = sgMode.add(new EnumSetting.Builder<GenerateMode>()
        .name("generate-mode")
        .description("The mode of trail generation.")
        .defaultValue(GenerateMode.Trail)
        .visible(() -> replayMode.get() == ReplayMode.Generate)
        .build()
    );

    private final Setting<Integer> searchAngle = sgMode.add(new IntSetting.Builder()
        .name("search-angle")
        .description("The angle deviation from the player's current yaw.")
        .defaultValue(20)
        .sliderRange(0,180)
        .max(180)
        .min(0)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Trail)
        .build()
    );

    private final Setting<Integer> minDistance = sgMode.add(new IntSetting.Builder()
        .name("min-distance")
        .description("The minimum block distance to move the player before checking another spot.")
        .defaultValue(500)
        .sliderRange(10, 10000)
        .min(10)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Trail)
        .build()
    );

    private final Setting<Integer> maxDistance = sgMode.add(new IntSetting.Builder()
        .name("max-distance")
        .description("The maximum block distance to move the player before checking another spot.")
        .defaultValue(500)
        .min(10)
        .sliderRange(10, 10000)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Trail)
        .build()
    );

    private final Setting<Boolean> angleOverlap = sgMode.add(new BoolSetting.Builder()
        .name("angle-overlap")
        .description("Generates the new facing angles from the original angle instead of the last one.")
        .defaultValue(true)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Trail)
        .build()
    );

    private final Setting<Integer> chunkDistance = sgMode.add(new IntSetting.Builder()
        .name("chunk-distance")
        .description("The distance between trail lines in chunks.")
        .defaultValue(24)
        .min(2)
        .max(60)
        .sliderRange(2, 60)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Spiral)
        .build()
    );

    private final Setting<Boolean> centerOnPlayer = sgMode.add(new BoolSetting.Builder()
        .name("center-on-player")
        .description("Centers the spiral on the player's chunk.")
        .defaultValue(true)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Spiral)
        .build()
    );

    private final Setting<Vector3d> spiralCenterPos = sgMode.add(new Vector3dSetting.Builder()
        .name("spiral-center")
        .description("Center block of the spiral.")
        .defaultValue(new Vector3d(0,0,0))
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Spiral && !centerOnPlayer.get())
        .noSlider()
        .build()
    );

    private final Setting<Integer> spiralWaypointQuantity = sgMode.add(new IntSetting.Builder()
        .name("waypoint-quantity")
        .description("The amount of waypoints to be pre-generated by the spiral algorithm.")
        .defaultValue(100)
        .min(1)
        .sliderRange(1, 400)
        .visible(() -> replayMode.get() == ReplayMode.Generate && generateMode.get() == GenerateMode.Spiral)
        .build()
    );

    private final Setting<Keybind> saveWaypoint = sgMode.add(new KeybindSetting.Builder()
        .name("save-waypoint-keybind")
        .description("The keybind to save a waypoint.")
        .defaultValue(Keybind.fromKey(GLFW_KEY_APOSTROPHE))
        .visible(() -> replayMode.get() == ReplayMode.Save || replayMode.get() == ReplayMode.Edit)
        .build()
    );

    private final Setting<Keybind> deleteWaypoint = sgMode.add(new KeybindSetting.Builder()
        .name("delete-waypoint-keybind")
        .description("The keybind to delete a waypoint.")
        .defaultValue(Keybind.fromKey(GLFW_KEY_SEMICOLON))
        .visible(() -> replayMode.get() == ReplayMode.Save || replayMode.get() == ReplayMode.Edit)
        .build()
    );

    private final Setting<Integer> deleteDistance = sgMode.add(new IntSetting.Builder()
        .name("delete-distance")
        .description("The maximum distance to delete a waypoint.")
        .defaultValue(deltaDistance.get())
        .min(1)
        .sliderRange(1, 20)
        .visible(() -> replayMode.get() == ReplayMode.Save || replayMode.get() == ReplayMode.Edit)
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
        .description("Renders the waypoint box when flying.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderBeams = sgRender.add(new BoolSetting.Builder()
        .name("render-beams")
        .description("Renders the beams of the waypoints when flying.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderLines = sgRender.add(new BoolSetting.Builder()
        .name("render-lines")
        .description("Renders the lines and arrows between waypoints when flying.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> sideColorBox = sgRender.add(new ColorSetting.Builder()
        .name("current-side-color")
        .description("The side color of the current waypoint.")
        .defaultValue(new SettingColor(16,144,106, 100))
        .visible(() -> renderWaypoints.get() || renderBeams.get() || renderLines.get())
        .build()
    );

    private final Setting<SettingColor> prevSideColorBox = sgRender.add(new ColorSetting.Builder()
        .name("previous-side-color")
        .description("The side color of the previous waypoints.")
        .defaultValue(new SettingColor(144,106,16, 100))
        .visible(() -> renderWaypoints.get() || renderBeams.get() || renderLines.get())
        .build()
    );

    private final Setting<SettingColor> nextSideColorBox = sgRender.add(new ColorSetting.Builder()
        .name("next-side-color")
        .description("The side color of the next waypoints.")
        .defaultValue(new SettingColor(106,16,144, 100))
        .visible(() -> renderWaypoints.get() || renderBeams.get() || renderLines.get())
        .build()
    );

    private final Setting<Boolean> occlusionCulling = sgRender.add(new BoolSetting.Builder()
        .name("occlusion-culling")
        .description("Doesn't render the beams behind blocks.")
        .defaultValue(false)
        .visible(() -> renderWaypoints.get() || renderBeams.get() || renderLines.get())
        .build()
    );

    private final Setting<Integer> arrows = sgRender.add(new IntSetting.Builder()
        .name("arrows")
        .description("The amount of arrows between waypoints.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 20)
        .visible(renderLines::get)
        .build()
    );

    private FlightData currentFlightData;
    private String flightFilename;
    private float originalAngle;
    private Vec3d currentWaypoint;
    private int currentWaypointIndex;
    private Vec3d centerSpiralBlock;
    private int firstWaypointIndex;
    private LocalTime startTime;
    private boolean exception = false;
    public static final String EMPTY_REPLAY_FOLDER_STRING = "No replays found.";
    public static final String SELECT_REPLAY_STRING = "Select a replay.";

    private void instantiateFlightData() {
        startTime = LocalTime.now();
        switch (replayMode.get()) {
            case ReplayMode.Generate:
                handleGeneration();
                break;
            case Save:
                currentFlightData = new FlightData(FlightStats.genDummyMetadata(ReplayMode.Save), new ArrayList<>());
                break;
            case Load, Edit:
                handleFileLoad();
                break;
        }
    }

    private void handleGeneration() {
        originalAngle = mc.player.getYaw();
        currentFlightData = new FlightData(FlightStats.genDummyMetadata(ReplayMode.Generate), new ArrayList<>());
        if (generateMode.get() == GenerateMode.Spiral) {
            centerSpiralBlock = (centerOnPlayer.get()) ? getFromChunkPos(mc.player.getChunkPos()) : getFromVector3d(spiralCenterPos.get());
            generateSpiralBundle(currentFlightData, chunkDistance.get(), centerSpiralBlock, spiralWaypointQuantity.get());
        } else {
            currentFlightData.addWaypoint(new Vec3d(mc.player.getX(), NULL_Y_VALUE, mc.player.getZ()));
        }
        currentWaypointIndex = 0;
        currentWaypoint = currentFlightData.getWaypoints().get(currentWaypointIndex);
    }

    private void handleFileLoad() {
        try {
            exception = false;
            currentFlightData = ReplayFileManager.loadReplay(replay.get(), rewind.get());
            flightFilename = replay.get();
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
            warning(" Couldn't find the replay file. (highlight)Stopping.");
            exception = true;
        } catch (Exception e) {
            error("Couldn't load the replay, maybe it's corrupted or has the wrong permissions. (highlight)Stopping.");
            exception = true;
        } finally {
            if (!exception) {
                if (loadMode.get() == LoadMode.Nearest)
                    currentWaypointIndex = currentFlightData.getNearestWaypointIndex();
                else currentWaypointIndex = 0;
                firstWaypointIndex = currentWaypointIndex;
                currentWaypoint = currentFlightData.getWaypoints().get(currentWaypointIndex);
            } else toggle();
        }
    }

    private void handleFileSave() {
        try {
            if (currentFlightData.getWaypoints().isEmpty()) {
                warning("Couldn't save the replay, since there aren't any waypoints. (highlight)Stopping.");
                exception = true;
                return;
            }
            String worldName = Utils.getWorldName().replaceAll("[<>:\"/\\\\|?*]", "_");
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String filename = (replayMode.get() == ReplayMode.Edit) ? flightFilename : worldName + "(" + date + ")";
            if (currentFlightData.getFlightStats().mode() == ReplayMode.Generate && replayMode.get() != ReplayMode.Edit) {
                currentFlightData.getWaypoints().removeLast();
                currentFlightData.addWaypoint(new Vec3d(mc.player.getPos().x, NULL_Y_VALUE, mc.player.getPos().z));
            }
            if (replayMode.get() != ReplayMode.Edit) currentFlightData.updateStats(startTime, date);
            ReplayFileManager.saveReplay(currentFlightData, filename);
            flightFilename = filename;
        } catch (Exception e) {
            error("Couldn't save the replay: " + e.getMessage() + " (highlight)Stopping.");
            exception = true;
        }
    }

    @Override
    public void onActivate() {
        instantiateFlightData();
        if (!autoEnableElytraExtras.get()) warning("You don't have ElytraExtras enabled, consider enabling it.");
        if (!Modules.get().isActive(ElytraExtras.class) && autoEnableElytraExtras.get()) {
            Modules.get().get(ElytraExtras.class).fixYaw.set(false);
            Modules.get().get(ElytraExtras.class).toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (Modules.get().isActive(ElytraExtras.class) && autoEnableElytraExtras.get())
            Modules.get().get(ElytraExtras.class).toggle();
        if (replayMode.get() != ReplayMode.Load) handleFileSave();
        if (exception) {exception = false; return;}
        if (flightStats.get()) {
            if (replayMode.get() != ReplayMode.Load) {
                FlightStatsManager.showStats(flightFilename, currentFlightData.getFlightStats());
            } else {
                FlightStatsManager.showCurrentStats(startTime, firstWaypointIndex, currentWaypointIndex, currentFlightData);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onKey(KeyEvent event) { handleKeyPress(event.key, event.action); }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseButton(MouseButtonEvent event) { handleKeyPress(event.button, event.action); }

    private void handleKeyPress(int keybind, KeyAction action) {
        if (action == KeyAction.Release) return;
        if (keybind == saveWaypoint.get().getValue()) {
            if (replayMode.get() == ReplayMode.Save) currentFlightData.addWaypoint(mc.player.getPos());
            if (replayMode.get() == ReplayMode.Edit) {
                Vec3d newPos = (currentFlightData.getFlightStats().mode() == ReplayMode.Generate) ?
                    new Vec3d(mc.player.getX(),NULL_Y_VALUE,mc.player.getZ()) : mc.player.getPos();
                currentFlightData.addWaypointWithEditMode(newPos);
            }
        }
        if (keybind == deleteWaypoint.get().getValue()) {
            if (replayMode.get() == ReplayMode.Save || replayMode.get() == ReplayMode.Edit && currentFlightData.getWaypoints().size() > 1) {
                int getNearestWaypointIndex = currentFlightData.getNearestWaypointIndex(deleteDistance.get());
                if (currentFlightData.getWaypoints().size() > 1 && getNearestWaypointIndex != -1) currentFlightData.removeWaypoint(getNearestWaypointIndex);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (replayMode.get() == ReplayMode.Save || replayMode.get() == ReplayMode.Edit) return;
        Vec3d playerPos = mc.player.getPos();
        double distanceToWaypoint = getDistance(currentWaypoint,playerPos,currentFlightData.getFlightStats().mode());
        if (distanceToWaypoint < deltaDistance.get()) {
            if (replayMode.get() == ReplayMode.Generate) {
                if (generateMode.get() == GenerateMode.Trail) {
                    if (angleOverlap.get()) currentFlightData.generateWaypoint(searchAngle.get(),originalAngle,minDistance.get(),maxDistance.get());
                    else currentFlightData.generateWaypoint(searchAngle.get(),mc.player.getYaw(),minDistance.get(),maxDistance.get());
                }
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
        if (occlusionCulling.get()) {
            event.renderer.triangles.depthTest = true;
            event.renderer.lines.depthTest = true;
        }
        for (int waypoint = 0; waypoint < currentFlightData.getWaypoints().size(); waypoint++) {
            Color color = (waypoint == currentWaypointIndex) ? sideColorBox.get() :
                (waypoint < currentWaypointIndex) ? prevSideColorBox.get() : nextSideColorBox.get();
            if (renderBeams.get()) WaypointRenderer.renderBeam(event.renderer, currentFlightData.getWaypoints().get(waypoint), color);
            if (renderWaypoints.get()) WaypointRenderer.renderWaypoint(event.renderer, currentFlightData.getWaypoints().get(waypoint), color);
            if (renderLines.get() && waypoint > 0) {
                Vec3d first = currentFlightData.getWaypoints().get(waypoint);
                Vec3d second = currentFlightData.getWaypoints().get(waypoint-1);
                WaypointRenderer.renderLine(event.renderer, first, second, color);
                WaypointRenderer.renderArrows(event.renderer, first, second, arrows.get(), color);
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
    private void onPacketRecieve(PacketEvent.Receive event) {
        if (this.disableOnDamage.get() && event.packet instanceof HealthUpdateS2CPacket packet) {
            if (this.mc.player.getHealth() - packet.getHealth() > 0.0F) this.toggle();
            info("Disabling...");
        }
        if (this.disableOnDamage.get() && event.packet instanceof DeathMessageS2CPacket packet) {
            this.toggle();
            info("Disabling...");
        }
    }
}

