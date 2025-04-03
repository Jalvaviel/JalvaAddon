package com.jalvaviel.addon.ChunkTrailer.FileManager;

import com.jalvaviel.addon.ChunkTrailer.FlightData.FlightData;

public interface IReplayFileManager {
    static void saveReplay(FlightData flightData, String filename) throws Exception {}

    static FlightData loadReplay(String filename, boolean rewind) throws Exception { return null; }

    static String[] getReplayFiles() { return null; }
}
