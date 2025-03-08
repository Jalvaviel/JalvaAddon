package com.jalvaviel.addon.ChunkTrailer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.jalvaviel.addon.modules.ChunkTrailer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.jalvaviel.addon.Addon.LOG;
import static com.jalvaviel.addon.utils.WaypointUtils.NULL_Y_VALUE;

public class ReplayFileManager {
    private static final Path REPLAYS_PATH = FabricLoader.getInstance().getGameDir().resolve("meteor-client/trail-replays");

    public static void saveReplay(FlightData flightData, String filename) throws Exception {
        if (Files.notExists(REPLAYS_PATH)) Files.createDirectory(REPLAYS_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String filepath = REPLAYS_PATH + "/" + filename;
        filepath += (filename.contains(".json")) ? "" : ".json";
        FileWriter writer = new FileWriter(filepath);
        gson.toJson(flightData, writer);
        writer.close();
    }

    public static FlightData loadReplay(String filename, boolean rewind) throws Exception {
        if (filename.contains(ChunkTrailer.EMPTY_REPLAY_FOLDER_STRING))
            throw new FileNotFoundException("Empty replay folder. ");
        if (!filename.contains(".json")) throw new IOException("Invalid format for file " + filename);
        FileReader reader = new FileReader(REPLAYS_PATH.resolve(filename).toFile());
        Gson gson = new Gson();
        FlightData flightData = gson.fromJson(reader, FlightData.class);
        if (rewind) Collections.reverse(flightData.getWaypoints());
        return flightData;
    }

    public static FlightData convertOldReplay(String filename) throws Exception {
        FileReader reader = new FileReader(REPLAYS_PATH.resolve(filename).toFile());
        Gson gson = new Gson();
        Vec3d[] waypointArray = gson.fromJson(reader, Vec3d[].class);
        ArrayList<Vec3d> waypoints = Arrays.stream(waypointArray).collect(Collectors.toCollection(ArrayList::new));
        ReplayMode replayMode = (waypoints.getFirst().getY() == NULL_Y_VALUE) ? ReplayMode.Generate : ReplayMode.Save;
        FlightData flightData = new FlightData(FlightMetadata.genDummyMetadata(replayMode), waypoints);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        flightData.updateMetadata(LocalTime.now(), date);
        saveReplay(flightData, filename);
        return flightData;
    }

    public static String[] getReplayFiles(){
        try {
            if (Files.notExists(REPLAYS_PATH)) Files.createDirectory(REPLAYS_PATH);
            String[] files = Files.list(REPLAYS_PATH)
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .map(path -> path.getFileName().toString())
                .toArray(String[]::new);
            return files.length == 0 ? new String[]{"No replays found."} : files;
        } catch (IOException e) {
            LOG.error("Couldn't access the replay files directory.");
        }
        return new String[]{"No replays found."};
    }
}
