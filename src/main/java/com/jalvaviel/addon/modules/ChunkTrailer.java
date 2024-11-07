package com.jalvaviel.addon.modules;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.utils.FileSetting;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkTrailer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private enum StepMode {
        Distance,
        Time,
        Replay
    }

    public ChunkTrailer() {
        super(Addon.CATEGORY, "chunk-trailer", "Generates random chunk trails");
    }

    private final Setting<StepMode> stepModeSetting = sgGeneral.add(new EnumSetting.Builder<StepMode>()
        .name("step-mode")
        .description("Mode of step")
        .defaultValue(StepMode.Distance)
        .build()
    );

    private final Setting<List<String>> replay = sgGeneral.add(new FileSetting.Builder<List<String>>()
        .path(replayDirectory)
        .format("json")
        .name("replay")
        .description("Select the replay file to replay")
        .build()
    );

    private final Setting<Integer> searchAngle = sgGeneral.add(new IntSetting.Builder()
        .name("search-angle")
        .description("The angle deviation from the player's current yaw")
        .defaultValue(45)
        .sliderRange(1,180)
        .visible(() -> stepModeSetting.get() != StepMode.Replay)
        .build()
    );

    private final Setting<Integer> distance = sgGeneral.add(new IntSetting.Builder()
        .name("distance-range")
        .description("The block distance to move the player before checking another spot")
        .defaultValue(500)
        .sliderRange(10, 10000)
        .visible(() -> stepModeSetting.get() == StepMode.Distance)
        .build()
    );

    private final Setting<Integer> time = sgGeneral.add(new IntSetting.Builder()
        .name("time-range")
        .description("The time to move the player before checking another spot in seconds")
        .defaultValue(60)
        .sliderRange(10, 3600)
        .visible(() -> stepModeSetting.get() == StepMode.Time)
        .build()
    );

    private final Setting<Boolean> saveReplay = sgGeneral.add(new BoolSetting.Builder()
        .name("save-replay")
        .description("Saves the replay as a distance with angles")
        .defaultValue(true)
        .visible(() -> stepModeSetting.get() == StepMode.Distance)
        .build()
    );

    private final Setting<Boolean> lockYaw = sgGeneral.add(new BoolSetting.Builder()
        .name("lock-yaw")
        .description("Locks the yaw of the player while flying")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoEnableElytraBoost = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-enable-elytra-boost")
        .description("Enables the ElytraBoostPlus module automatically")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> flightStats = sgGeneral.add(new BoolSetting.Builder()
        .name("flight-stats")
        .description("Shows flight stats when done flying")
        .defaultValue(false)
        .build()
    );

    int tickTimer;
    int timer;
    int oldPlayerYaw;
    int playerYaw;
    Vec3d firstPlayerPos;
    Vec3d lastPlayerPos;
    int cumulativeDistance;
    FlightReplay flightReplay;
    private static final Path replayDirectory;

    static {
        try {
            replayDirectory = FlightReplay.getFlightReplayPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onActivate() {
        assert mc.player != null;

        if (!Modules.get().isActive(ElytraBoostPlus.class)) {
            if (autoEnableElytraBoost.get()) {
                Modules.get().get(ElytraBoostPlus.class).toggle();
            } else {
                warning("You don't have ElytraBoostPlus enabled, consider enabling it.");
            }
        }
        tickTimer = 0;
        timer = 0;
        cumulativeDistance = 0;
        oldPlayerYaw = (int) mc.player.getYaw();
        playerYaw = (int) mc.player.getYaw();
        firstPlayerPos = mc.player.getPos();
        lastPlayerPos = mc.player.getPos();
        flightReplay = new FlightReplay(distance.get(), new ArrayList<>(List.of(oldPlayerYaw)),firstPlayerPos);
    }

    @Override
    public void onDeactivate() {
        if (Modules.get().isActive(ElytraBoostPlus.class) && autoEnableElytraBoost.get()) {
            Modules.get().get(ElytraBoostPlus.class).toggle();
        }
        if (saveReplay.get()) {
            try {
                flightReplay.saveFlightData();
            } catch (IOException e) {
                error("Couldn't save replay file...");
            }
        }
        if (flightStats.get()) {
            assert mc.player != null;
            switch (stepModeSetting.get()) {
                case Distance:
                    info("Absolute distance flown: (highlight)%d blocks", (int) firstPlayerPos.distanceTo(mc.player.getPos()));
                    info("Cumulative distance flown: (highlight)%d blocks", Math.max((int) firstPlayerPos.distanceTo(mc.player.getPos()), cumulativeDistance));
                    break;

                case Time:
                    info("Time flown: (highlight)%d seconds", (int) (tickTimer / 20));
                    break;

                default:
                    break;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        assert mc.player != null;
        if (lockYaw.get()) mc.player.setYaw(playerYaw);
        switch (stepModeSetting.get()) {
            case Distance:
                if (lastPlayerPos.distanceTo(mc.player.getPos()) >= distance.get()) {
                    cumulativeDistance += (int) lastPlayerPos.distanceTo(mc.player.getPos());
                    changeYaw();
                    lastPlayerPos = mc.player.getPos();
                }
                break;

            case Time:
                tickTimer++;
                if (tickTimer % 20 == 0) timer++;
                if (timer >= time.get()) {
                    changeYaw();
                    timer = 0;
                }
                break;

            default:
                break;
        }
    }

    private void changeYaw(){
        assert mc.player != null;
        playerYaw = ThreadLocalRandom.current().nextInt(oldPlayerYaw-searchAngle.get(), oldPlayerYaw+searchAngle.get());
        mc.player.setYaw(playerYaw);
        if (saveReplay.get()) flightReplay.yawPitches.add(playerYaw);
    }

    private static class FlightReplay {
        @SerializedName("distance")
        protected int distance;

        @SerializedName("yaw_pitches")
        protected List<Integer> yawPitches;

        @SerializedName("initial_pos")
        protected Vec3d initialPos;

        protected FlightReplay(int distance, List<Integer> yawPitches, Vec3d initialPos) {
            this.distance = distance;
            this.yawPitches = new ArrayList<>(yawPitches);
            this.initialPos = initialPos;
        }

        protected void saveFlightData() throws IOException {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter(getFlightReplayPath().toString() + "/"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-(HH-mm-ss)")) + ".json");
            gson.toJson(this, writer);
            writer.close();
        }

        protected static FlightReplay loadFlightData(Path filePath) throws IOException {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(filePath.toString())) {
                return gson.fromJson(reader, FlightReplay.class);
            }
        }

        protected static Path getFlightReplayPath() throws IOException {
            Path replayDirectory = FabricLoader.getInstance().getGameDir().resolve("meteor-client/flight-replays");
            if (!Files.exists(replayDirectory)) Files.createDirectory(replayDirectory);
            return replayDirectory;
        }
    }
}
