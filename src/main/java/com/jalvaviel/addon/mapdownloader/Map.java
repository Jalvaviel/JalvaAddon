package com.jalvaviel.addon.mapdownloader;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Map {
    ItemStack mapStack;
    byte [] pixelData = null;
    BufferedImage bufferedMap = null;
    int width;
    int height;
    UUID imageID = null;

    enum

    public Map(ItemStack mapStack, int width, int height) {
        this.mapStack = mapStack;
        this.width = width;
        this.height = height;
    }

    private byte @Nullable [] getPixelDataFromMap(ItemStack itemStack) {
        MapState mapState = FilledMapItem.getMapState(itemStack, mc.world);
        // Get the pixels of the map in its MapState.

        if (mapState != null && mapState.colors != null && mapState.colors.length == 16384) {
            // Ensure mapState is not null and has the expected data size (128x128)
            return mapState.colors;
        } else {
            if (debug.get()) {
                ChatUtils.sendMsg(Text.of("Couldn't retrieve the pixel data from the map."));}
            return null;
        }
    }
}
