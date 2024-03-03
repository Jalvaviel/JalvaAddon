package com.jalvaviel.addon.utils;

import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static com.jalvaviel.addon.Addon.LOG;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Utils {
    static final int PIXELS_IN_MAP = 128;
    static final int HALF_OFFSET = 7;
    static final int OFFSET = 14;

    /**
     * UUID generation
     **/

    public static UUID generateBufferedImageIdentifier(BufferedImage bufferedImage){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            LOG.warn("Couldn't store the image UUID");
        }
        return UUID.nameUUIDFromBytes(baos.toByteArray());
    }

    /**
     * Folder and .png creation
     **/

    public static @NotNull String getFolderPath(boolean isServer, String folderString){
        if(isServer) {
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString + "\\" + Objects.requireNonNull(mc.getCurrentServerEntry()).name;
        }else{
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString + "\\" + Objects.requireNonNull(mc.getServer()).getSaveProperties().getLevelName();
        }
    }

    public static void createWorldFolder(String folderPath) {
        File mapImagesFolder = new File(folderPath);
        if (!mapImagesFolder.exists()) {
            if(!mapImagesFolder.mkdirs()) {
                LOG.warn("Couldn't create the map folder, maybe it does exist already?");
            };
        }
    }

    public static void writeImageToFolder(BufferedImage bufferedImage, String fullPath){
        try {
            File output = new File(fullPath);
            ImageIO.write(bufferedImage, "png", output);
        } catch (IOException e) {
            LOG.warn("Couldn't store the image");
        }
    }

    public static void writeCanvasToFolder(CanvasData canvasData, String fullPath){
        writeImageToFolder(canvasData.bufferedCanvas, fullPath);
    }

    public enum Axis{
        X, Y, Z, UNKNOWN;
    }


    public static BufferedImage rotateImage(BufferedImage img, double angle) {

        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}
