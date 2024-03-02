package com.jalvaviel.addon.utils;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FramedCanvas extends Canvas {
    private BufferedImage canvasBG;
    public FramedCanvas(ArrayList<Map> mapMatrix, CanvasType canvasType) {
        super(mapMatrix, canvasType);
        getCanvasFrame(canvasType);
        setCanvasFrame();
    }

    public FramedCanvas(ArrayList<Map> mapMatrix, int tileWidth, int tileHeight) {
        super(mapMatrix, tileWidth, tileHeight);
        FramedCanvasGenerator customFrame = new FramedCanvasGenerator(tileWidth, tileHeight);
        getCanvasFrame(CanvasType.CUSTOM);
        setCanvasFrame();
    }

    private void getCanvasFrame(CanvasType canvasType){
        String location = "jalvaaddon:textures/single_bg.png";
        switch(canvasType){
            case PLAYER_INVENTORY:
                location = "jalvaaddon:textures/player_inventory_bg.png";
                break;
            case CHEST:
                location = "jalvaaddon:textures/chest_bg.png";
                break;
            case DOUBLE_CHEST:
                location = "jalvaaddon:textures/double_chest_bg.png";
                break;
            case DISPENSER:
                location = "jalvaaddon:textures/dispenser_bg.png";
                break;
            case CUSTOM:
                location = "jalvaaddon:textures/custom_bg.png";
                break;
            default:
                break;
        }
        try {
            Identifier identifier = Identifier.tryParse(location);
            Optional<Resource> resource = mc.getResourceManager().getResource(identifier);
            byte[] is = resource.get().getInputStream().readAllBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(is);
            this.canvasBG = ImageIO.read(bis);
        }
        catch(IOException e){
            //super.generateBlankImage();
        }
    }
    private void setCanvasFrame(){
        BufferedImage resultImage = new BufferedImage(this.pixelWidth+14, this.pixelHeight+14, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultImage.createGraphics();
        graphics.drawImage(this.canvasBG, 0, 0, null);
        graphics.drawImage(this.bufferedCanvas, 7, 7, null);
        graphics.dispose();
        this.bufferedCanvas = resultImage;
    }
}
