package com.jalvaviel.addon.utils;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ProceduralFrameGenerator {
    enum SubImageID {
        TOP(0),
        LEFT(1),
        RIGHT(2),
        BOT(3),
        TOP_LEFT(4),
        TOP_RIGHT(5),
        BOT_LEFT(6),
        BOT_RIGHT(7),
        BACKGROUND(8),
        MAX(9);

        protected final int value;
        SubImageID(int value){
            this.value = value;
        }
    }
    private int width = 0;
    private int height = 0;
    private int tileWidth = 9;
    private int tileHeight = 9;
    private int offset = 14;

    private BufferedImage getCanvasAtlas() {
        String location = "jalvaaddon:textures/map_atlas.png";
        BufferedImage canvasAtlas = null;
        try {
            Identifier identifier = Identifier.tryParse(location);
            Optional<Resource> resource = mc.getResourceManager().getResource(identifier);
            byte[] is = resource.get().getInputStream().readAllBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(is);
            canvasAtlas = ImageIO.read(bis);
        } catch (IOException e) {
            LOG.debug("Couldn't generate canvas from map atlas");
        }
        return canvasAtlas;
    }

    public ProceduralFrameGenerator(int tileWidth, int tileHeight) {
        BufferedImage canvasAtlas = getCanvasAtlas();
        BufferedImage[] bufferedSubImages = getSubImages(canvasAtlas);
        BufferedImage bufferedCanvas = generateBackgroundCanvas(tileWidth, tileHeight, bufferedSubImages);
    }

    private BufferedImage[] getSubImages(BufferedImage canvasAtlas) {
        BufferedImage bufferedImages[] = new BufferedImage[SubImageID.MAX.value];
        bufferedImages[SubImageID.TOP_LEFT.value] = canvasAtlas.getSubimage(0, 0, 7, 7);
        bufferedImages[SubImageID.TOP_RIGHT.value] = canvasAtlas.getSubimage(137, 0, 7, 7);
        bufferedImages[SubImageID.BOT_LEFT.value] = canvasAtlas.getSubimage(0, 137, 7, 7);
        bufferedImages[SubImageID.BOT_RIGHT.value] = canvasAtlas.getSubimage(137, 137, 7, 7);
        bufferedImages[SubImageID.TOP.value] = canvasAtlas.getSubimage(8, 0, 128, 7);
        bufferedImages[SubImageID.BOT.value] = canvasAtlas.getSubimage(8, 137, 128, 7);
        bufferedImages[SubImageID.LEFT.value] = canvasAtlas.getSubimage(0, 8, 7, 128);
        bufferedImages[SubImageID.RIGHT.value] = canvasAtlas.getSubimage(137, 8, 7, 128);
        bufferedImages[SubImageID.BACKGROUND.value] = canvasAtlas.getSubimage(8, 8, 128, 128);
        return bufferedImages;
    }
    private BufferedImage generateBackgroundCanvas(int tileWidth, int tileHeight, BufferedImage[] bufferedAtlasImages) {
        final int halfOffset = 7;
        int offset = halfOffset * 2;
        width = (Utils.PIXELS_IN_MAP * tileWidth) + offset;
        height = (Utils.PIXELS_IN_MAP * tileHeight) + offset;
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultImage.createGraphics();

        // Draw top and bottom edges
        for (int x = 0; x < Utils.PIXELS_IN_MAP*tileWidth; x += Utils.PIXELS_IN_MAP) { // Adjusted width
            graphics.drawImage(bufferedAtlasImages[SubImageID.TOP.value], x+halfOffset, 0, null);
            graphics.drawImage(bufferedAtlasImages[SubImageID.BOT.value], x+halfOffset, Utils.PIXELS_IN_MAP*tileHeight+halfOffset, null); // TODO
        }

        // Draw left and right edges
        for (int y = 0; y < Utils.PIXELS_IN_MAP*tileHeight; y += Utils.PIXELS_IN_MAP) { // Adjusted height
            graphics.drawImage(bufferedAtlasImages[SubImageID.LEFT.value], 0, y+halfOffset, null);
            graphics.drawImage(bufferedAtlasImages[SubImageID.RIGHT.value], Utils.PIXELS_IN_MAP*tileWidth+halfOffset, y+halfOffset, null); // TODO
        }

        // Draw background
        for (int i = 0; i < Utils.PIXELS_IN_MAP*tileHeight; i += Utils.PIXELS_IN_MAP) { // Adjusted height
            for (int j = 0; j < Utils.PIXELS_IN_MAP*tileWidth; j += Utils.PIXELS_IN_MAP) { // Adjusted width
                graphics.drawImage(bufferedAtlasImages[SubImageID.BACKGROUND.value], j+halfOffset, i+halfOffset, null);
            }
        }

        // Draw corners
        graphics.drawImage(bufferedAtlasImages[SubImageID.TOP_LEFT.value], 0, 0, null);
        graphics.drawImage(bufferedAtlasImages[SubImageID.TOP_RIGHT.value], width-halfOffset, 0, null);
        graphics.drawImage(bufferedAtlasImages[SubImageID.BOT_LEFT.value], 0, height-halfOffset, null);
        graphics.drawImage(bufferedAtlasImages[SubImageID.BOT_RIGHT.value], width-halfOffset, height-halfOffset, null);
        graphics.dispose();

        /*
        try {
            File outputDir = new File(FabricLoader.getInstance().getGameDir().resolve("maps").toString());
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Failed to create output directory");
            }
            File outputFile = new File(outputDir, filename+".png");
            ImageIO.write(resultImage, "png", outputFile);
        } catch (IOException e) {
            Addon.LOG.warn("Failed to store the generated canvas: {}", e.getMessage());
        }
        */
        return resultImage;
    }


}
