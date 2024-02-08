package com.jalvaviel.addon.modules;

import net.fabricmc.loader.api.FabricLoader;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MapImage {
    private UUID uuid;
    private byte [] mapByteStream;
    private BufferedImage bufferedMap;
    private String mapPath = String.valueOf(FabricLoader.getInstance().getGameDir());
    private boolean mapBG = false;
    private boolean isServer = false;
    private int X = 128;
    private int Y = 128;

    public MapImage(UUID uuid, byte[] mapByteStream, BufferedImage bufferedMap, String mapPath, boolean mapBG, boolean isServer, int x, int y) {
        this.uuid = uuid;
        this.mapByteStream = mapByteStream;
        this.bufferedMap = bufferedMap;
        this.mapPath = mapPath;
        this.mapBG = mapBG;
        this.isServer = isServer;
        X = x;
        Y = y;
    }
}
