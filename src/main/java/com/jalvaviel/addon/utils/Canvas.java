package com.jalvaviel.addon.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static com.jalvaviel.addon.Addon.LOG;

public class Canvas {
    public int tileWidth = 0;
    public int tileHeight = 0;
    public int pixelWidth = 0;
    public int pixelHeight = 0;
    public ArrayList<Map> mapMatrix = new ArrayList<>();
    public UUID canvasID = null;
    public BufferedImage bufferedCanvas = null;

    public Canvas(ArrayList<Map> mapMatrix, int tileWidth, int tileHeight){
        this.mapMatrix = mapMatrix;
        generateCanvas(CanvasType.CUSTOM);
        drawCanvas();
        generateCanvasIdentifier();
    }

    public Canvas(ArrayList<Map> mapMatrix, CanvasType canvasType){
        this.mapMatrix = mapMatrix;
        generateCanvas(canvasType);
        drawCanvas();
        generateCanvasIdentifier();
    }

    private void generateCanvas(CanvasType canvasType){ // TODO change magic numbers
        switch(canvasType){
            case PLAYER_INVENTORY:
                this.pixelWidth = 128 * 9;
                this.pixelHeight = 128 * 4;
                break;
            case CHEST:
                this.pixelWidth = 128 * 9;
                this.pixelHeight = 128 * 3;
                break;
            case DOUBLE_CHEST:
                this.pixelWidth = 128 * 9;
                this.pixelHeight = 128 * 6;
                break;
            case DISPENSER:
                this.pixelWidth = 128 * 3;
                this.pixelHeight = 128 * 3;
                break;
            case SINGLE:
                this.pixelWidth = 128;
                this.pixelHeight = 128;
                break;
            case CUSTOM:
                this.pixelWidth = 128 * this.tileWidth;
                this.pixelHeight = 128 * this.tileHeight;
                break;
            default:
                break;
        }
    }

    private void drawCanvas() {
        BufferedImage resultImage = new BufferedImage(this.pixelWidth, this.pixelHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultImage.createGraphics();
        int mapIndex = 0;
        /*
        for (int y = 0; y < this.pixelHeight; y += 128) { // TODO don't do it two dimensional, one dimensional with map coords (offset)
            for (int x = 0; x < this.pixelWidth; x += 128) {
                if (mapIndex < mapMatrix.size()) {
                    Map map = mapMatrix.get(mapIndex++);
                    graphics.drawImage(map.bufferedMap, x, y, null);
                } else {
                    break;
                }
            }
        }
         */

        graphics.dispose();
        this.bufferedCanvas = resultImage;
    }

    private void generateCanvasIdentifier(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedCanvas, "png", baos);
        } catch (IOException e) {
            LOG.warn("Couldn't store the canvas UUID");
        }
        this.canvasID = UUID.nameUUIDFromBytes(baos.toByteArray());
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
