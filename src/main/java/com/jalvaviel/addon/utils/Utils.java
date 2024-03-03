package com.jalvaviel.addon.utils;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
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

    public static void writeMapToFolder(Map map, String fullPath){
        writeImageToFolder(map.bufferedMap, fullPath);
    }
    public static void writeCanvasToFolder(CanvasData canvasData, String fullPath){
        writeImageToFolder(canvasData.bufferedCanvas, fullPath);
    }
}
