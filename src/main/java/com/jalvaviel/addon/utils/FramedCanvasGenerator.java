package com.jalvaviel.addon.utils;

import com.jalvaviel.addon.Addon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FramedCanvasGenerator {
    private BufferedImage canvasAtlas;
    private int width = 0;
    private int height = 0;
    private int tileWidth = 9;
    private int tileHeight = 9;
    private int offset = 14;
    private BufferedImage top;
    private BufferedImage bot;
    private BufferedImage left;
    private BufferedImage right;
    private BufferedImage topLeft;
    private BufferedImage topRight;
    private BufferedImage botLeft;
    private BufferedImage botRight;
    private BufferedImage background;

    private void getCanvasAtlas() {
        String location = "jalvaaddon:textures/map_atlas.png";
        try {
            Identifier identifier = Identifier.tryParse(location);
            Optional<Resource> resource = mc.getResourceManager().getResource(identifier);
            byte[] is = resource.get().getInputStream().readAllBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(is);
            canvasAtlas = ImageIO.read(bis);
        } catch (IOException e) {
            LOG.debug("Couldn't generate canvas from map atlas");
        }
    }

    public FramedCanvasGenerator(Canvas.CanvasType canvasType) {
        getCanvasAtlas();
        getSubImages();
        generateBackgroundCanvas(9, 4, "player_inventory_bg");
        generateBackgroundCanvas(9, 3, "chest_bg");
        generateBackgroundCanvas(9, 6, "double_chest_bg");
        generateBackgroundCanvas(3, 3, "dispenser_bg");
        generateBackgroundCanvas(1, 1, "single_bg");
    }

    private void getSubImages() {
        topLeft = canvasAtlas.getSubimage(0, 0, 7, 7);
        topRight = canvasAtlas.getSubimage(137, 0, 7, 7);
        botLeft = canvasAtlas.getSubimage(0, 137, 7, 7);
        botRight = canvasAtlas.getSubimage(137, 137, 7, 7);
        top = canvasAtlas.getSubimage(8, 0, 128, 7);
        bot = canvasAtlas.getSubimage(8, 137, 128, 7);
        left = canvasAtlas.getSubimage(0, 8, 7, 128);
        right = canvasAtlas.getSubimage(137, 8, 7, 128);
        background = canvasAtlas.getSubimage(8, 8, 128, 128);
    }
    private void generateBackgroundCanvas(int tileWidth, int tileHeight, String filename) {
        offset = 14;
        width = (128 * tileWidth) + offset;
        height = (128 * tileHeight) + offset;
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultImage.createGraphics();

        // Draw top and bottom edges
        for (int x = 0; x < 128*tileWidth; x += 128) { // Adjusted width
            graphics.drawImage(top, x+7, 0, null);
            graphics.drawImage(bot, x+7, 128*tileHeight+7, null); // TODO
        }

        // Draw left and right edges
        for (int y = 0; y < 128*tileHeight; y += 128) { // Adjusted height
            graphics.drawImage(left, 0, y+7, null);
            graphics.drawImage(right, 128*tileWidth+7, y+7, null); // TODO
        }

        // Draw background
        for (int i = 0; i < 128*tileHeight; i += 128) { // Adjusted height
            for (int j = 0; j < 128*tileWidth; j += 128) { // Adjusted width
                graphics.drawImage(background, j+7, i+7, null);
            }
        }

        // Draw corners
        graphics.drawImage(topLeft, 0, 0, null);
        graphics.drawImage(topRight, width-7, 0, null);
        graphics.drawImage(botLeft, 0, height-7, null);
        graphics.drawImage(botRight, width-7, height-7, null);
        graphics.dispose();

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
    }


}
