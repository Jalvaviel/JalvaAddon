package com.jalvaviel.addon.utils;

import net.minecraft.block.MapColor;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import java.awt.image.BufferedImage;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Map {
    private ItemStack mapStack;
    private byte [] pixelData = null;
    public BufferedImage bufferedMap;
    protected int width;
    protected int height;
    public UUID imageID = null;



    public Map(ItemStack mapStack, int width, int height) { // Constructor for arbitrary width and height
        this.mapStack = mapStack;
        this.width = width;
        this.height = height;
        this.bufferedMap = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        generateMap();
    }

    public Map(ItemStack mapStack) {
        this(mapStack,128,128);
    }

    public void generateMap(){
        getPixelDataFromMap();
        convertMap();
        generateImageIdentifier();
    }

    private void getPixelDataFromMap() {
        MapState mapState = FilledMapItem.getMapState(this.mapStack, mc.world); // Get the pixels of the map in its MapState.
        if (mapState == null || mapState.colors == null || mapState.colors.length != 128 * 128) { // Ensure it's not empty and has the correct dimensions.
            generateBlankImage();
        }
        this.pixelData = mapState.colors;
    }

    private void convertMap() {
        for (int i = 0; i < this.width; i++) { // Rows
            for (int j = 0; j < this.height; j++) { // Columns
                byte byteColor = this.pixelData[j * this.width + i]; // this.pixelData[i + j * this.height]
                int intColor = MapColor.getRenderColor(byteColor);
                this.bufferedMap.setRGB(i, j, bgrToArgb(intColor)); // Trust me, I've tried by using a BufferedImage.TYPE_INT_BGR, but this is a workaround.
            } // Problem here TODO ( Overload in FramedMap, and add an offset of 14 there when drawing the image
        }
    }

    protected void generateBlankImage(){
        BufferedImage blankMap = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < this.width; i++){ // Rows
            for (int j = 0; j < this.height; j++){ // Columns
                int rgbValue = 0xD6BE96; // Map texture background color
                blankMap.setRGB(i, j, rgbValue);
            }
        }
        this.bufferedMap = blankMap;
    }

    private int bgrToArgb(int bgrColor) {
        int alpha = (bgrColor == 0) ? 0 : 0xFF; // If bgrColor is black (0), set alpha to 0; otherwise, set it to 255
        int blue = (bgrColor >> 16) & 0xFF;
        int green = (bgrColor >> 8) & 0xFF;
        int red = bgrColor & 0xFF;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private void generateImageIdentifier(){
        this.imageID = UUID.nameUUIDFromBytes(this.pixelData);
    }
}
