package com.jalvaviel.addon.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.UUID;

public class Canvas {
    int width = 0;
    int height = 0;
    ArrayList<Map> mapMatrix = new ArrayList<>();
    UUID canvasID = null;
    BufferedImage bufferedCanvas = null;

    public Canvas(ArrayList<Map> mapMatrix, CanvasType canvasType){
        this.mapMatrix = mapMatrix;
        generateCanvas(canvasType);
        drawCanvas();
    }

    private void generateCanvas(CanvasType canvasType){
        switch(canvasType){
            case PLAYER_INVENTORY:
                this.width = 128 * 9;
                this.height = 128 * 4;
                break;
            case CHEST:
                this.width = 128 * 9;
                this.height = 128 * 3;
                break;
            case DOUBLE_CHEST:
                this.width = 128 * 9;
                this.height = 128 * 6;
                break;
            case DISPENSER:
                this.width = 128 * 3;
                this.height = 128 * 3;
                break;
            case SINGLE:
                this.width = 128;
                this.height = 128;
                break;
            case CUSTOM:
                // TODO
                break;
            default:
                break;
        }
    }

    private void drawCanvas() {
        BufferedImage resultImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultImage.createGraphics();
        int mapIndex = 0;
        for (int y = 0; y < this.height; y += 128) {
            for (int x = 0; x < this.width; x += 128) {
                if (mapIndex < mapMatrix.size()) {
                    Map map = mapMatrix.get(mapIndex++);
                    graphics.drawImage(map.bufferedMap, x, y, null);
                } else {
                    break;
                }
            }
        }
        graphics.dispose();
        this.bufferedCanvas = resultImage;
    }

    public enum CanvasType{
        CUSTOM,
        PLAYER_INVENTORY,
        CHEST,
        DOUBLE_CHEST,
        DISPENSER,
        SINGLE
    }
}
