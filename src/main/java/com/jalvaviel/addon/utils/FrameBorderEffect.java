package com.jalvaviel.addon.utils;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;


import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FrameBorderEffect {
    int frameMarginPixels = 14;
    public BufferedImage getFrameImageFromType(CanvasData canvasData){
        String location = "jalvaaddon:textures/single_bg.png";
        BufferedImage bgImage = null;
        switch(canvasData.canvasType){
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
            bgImage = ImageIO.read(bis);
        }
        catch(IOException e){
            //super.generateBlankImage(); //TODO use the procedural generator to generate a default one.
            ProceduralFrameGenerator customFrameGenerator = new ProceduralFrameGenerator(canvasData.widthInTiles,canvasData.heightInTiles);
            customFrameGenerator
        }
        return bgImage;
    }
    public CanvasData addFrameToCanvas(CanvasData canvasData){
        canvasData.canvasType
    }
}
