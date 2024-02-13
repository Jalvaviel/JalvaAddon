package com.jalvaviel.addon.utils;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FramedMap extends Map {
    private BufferedImage mapBG;

    public FramedMap(ItemStack mapStack, int width, int height) {
        super(mapStack, width, height);
        getMapBG();
        setMapFrame();
    }

    public FramedMap(ItemStack mapStack) {
        this(mapStack, 128, 128);
    }

    private void getMapBG(){
        String location = "jalvaaddon:textures/map_background.png";
        try {
            Identifier identifier = Identifier.tryParse(location);
            Optional<Resource> resource = mc.getResourceManager().getResource(identifier);
            byte[] is = resource.get().getInputStream().readAllBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(is);
            this.mapBG = ImageIO.read(bis);
        }
        catch(IOException e){
            super.generateBlankImage();
        }
    }

    private void setMapFrame(){
        BufferedImage resultImage = new BufferedImage(this.width+14, this.height+14, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resultImage.createGraphics();
        graphics.drawImage(this.mapBG, 0, 0, null);
        graphics.drawImage(this.bufferedMap, 7, 7, null);
        graphics.dispose();
        this.bufferedMap = resultImage;
    }
}
