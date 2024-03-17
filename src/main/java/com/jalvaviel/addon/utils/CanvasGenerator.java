package com.jalvaviel.addon.utils;

import net.minecraft.util.math.Direction;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.jalvaviel.addon.Addon.LOG;
public class CanvasGenerator {
    private static CanvasGenerator canvasGenerator;
    public CanvasData generateCanvasFromMapMatrix(Map[][] mapMatrix, CanvasType canvasType) {
        int tileWidth = mapMatrix.length;
        int tileHeight = mapMatrix[0].length;
        BufferedImage bufferedCanvas = new BufferedImage(tileWidth * Utils.PIXELS_IN_MAP, tileHeight * Utils.PIXELS_IN_MAP, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bufferedCanvas.createGraphics();
        for (int i = 0; i < tileHeight; i++) {
            for (int j = 0; j < tileWidth; j++) {
                Map currentMap = mapMatrix[j][i];
                if (currentMap != null) {
                    int flippedImageIndex = (tileHeight - i) * Utils.PIXELS_IN_MAP;

                    int positionX = j * Utils.PIXELS_IN_MAP;
                    int positionY = flippedImageIndex-Utils.PIXELS_IN_MAP;

                    if(currentMap.mapFacing == Direction.NORTH || currentMap.mapFacing == Direction.EAST){
                        positionX = (tileWidth - j) * Utils.PIXELS_IN_MAP;
                        positionX = positionX-Utils.PIXELS_IN_MAP;
                    }
                    graphics.drawImage(currentMap.bufferedMap, positionX, positionY, null);
                }
            }
        }
        graphics.dispose();

        UUID uuid = Utils.generateBufferedImageIdentifier(bufferedCanvas);

        return new CanvasData(bufferedCanvas, uuid, canvasType, tileWidth, tileHeight);
    }

    public static CanvasGenerator getCanvasGenerator(){
        if(canvasGenerator == null){
            canvasGenerator = new CanvasGenerator();
        }
        return canvasGenerator;
    }
}

