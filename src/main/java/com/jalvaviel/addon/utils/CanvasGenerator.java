package com.jalvaviel.addon.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.jalvaviel.addon.Addon.LOG;
public class CanvasGenerator{
    int pixelDimensions = 128;

    public CanvasData generateCanvasFromMapMatrix(Map[][] mapMatrix, CanvasType canvasType) {
        int tileWidth = mapMatrix.length;
        int tileHeight = mapMatrix[0].length;
        BufferedImage bufferedCanvas = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bufferedCanvas.createGraphics();

        for (int i = 0; i < tileHeight; i++) {
            for (int j = 0; j < tileWidth; j++) {
                Map currentMap = mapMatrix[j][i];
                if (currentMap != null) {
                    graphics.drawImage(currentMap.bufferedMap, j * pixelDimensions, i * pixelDimensions, null);
                }
            }
        }
        graphics.dispose();

        UUID uuid = Utils.generateBufferedImageIdentifier(bufferedCanvas);

        return new CanvasData(bufferedCanvas, uuid, canvasType, tileWidth, tileHeight);
    }
}


/*
    public Canvas(Map[][] mapMatrix){
        this.mapMatrix = mapMatrix;
        this.tileWidth = mapMatrix.length;
        this.tileHeight = mapMatrix[0].length;
        generateCanvas(CanvasType.CUSTOM);
        drawCanvas();
        generateCanvasIdentifier();
    }

    public Canvas(Map[][] mapMatrix, CanvasType canvasType){
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


        graphics.dispose();
        this.bufferedCanvas = resultImage;
    }




}

 */
