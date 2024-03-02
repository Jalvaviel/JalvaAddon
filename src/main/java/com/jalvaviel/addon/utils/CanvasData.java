package com.jalvaviel.addon.utils;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class CanvasData {
    public BufferedImage bufferedCanvas;
    public UUID canvasID;
    public CanvasType canvasType;
    public int widthInTiles;
    public int heightInTiles;
    public CanvasData(BufferedImage bufferedCanvas, UUID canvasID, CanvasType canvasType, int widthInTiles, int heightInTiles){
        this.bufferedCanvas = bufferedCanvas;
        this.canvasID = canvasID;
        this.canvasType = canvasType;
        this.widthInTiles = widthInTiles;
        this.heightInTiles = heightInTiles;
    }
    public CanvasData(BufferedImage bufferedCanvas, UUID canvasID, CanvasType canvasType){
        this(bufferedCanvas, canvasID, canvasType, 1, 1);
    }
}
