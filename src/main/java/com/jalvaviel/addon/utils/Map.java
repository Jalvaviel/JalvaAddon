package com.jalvaviel.addon.utils;

import net.minecraft.block.MapColor;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.Direction;

import java.awt.image.BufferedImage;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Map {
    private ItemStack mapStack = null;
    private byte [] pixelData = null;
    public BufferedImage bufferedMap;
    protected int width;
    protected int height;
    public UUID imageID = null;
    protected Direction mapFacing;
    protected int rotation = 0;

    public Map(ItemStack mapStack, int width, int height, Direction mapFacing, int rotation) { // Constructor for arbitrary width and height
        this.mapStack = mapStack;
        this.width = width;
        this.height = height;
        this.bufferedMap = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        this.mapFacing = mapFacing;
        this.rotation = rotation;
        generateMap();
    }

    public Map(ItemStack mapStack, Direction mapFacing, int rotation) {
        this(mapStack,128,128, mapFacing, rotation);
    }
    public Map(ItemStack mapStack) {
        this(mapStack,128,128, Direction.UP,0);
    }
    public Map(boolean isEmptyMap) {
        this.width = 128;
        this.height = 128;
        generateBlankImage();
        this.imageID = UUID.fromString("DEADBEEF-0000-0000-0000-000000000000");
    }

    public void generateMap(){
        getPixelDataFromMap();
        convertMap();
        this.bufferedMap = Utils.rotateImage(bufferedMap, rotation*90);
        generateImageIdentifier();
    }

    private void getPixelDataFromMap() {
        MapState mapState = FilledMapItem.getMapState(this.mapStack, mc.world); // Get the pixels of the map in its MapState.
        if (mapState == null || mapState.colors == null || mapState.colors.length != 128 * 128) { // Ensure it's not empty and has the correct dimensions.
            generateBlankImage();
        } else {
            this.pixelData = mapState.colors;
        }
    }

    private void convertMap() {
        for (int i = 0; i < this.width; i++) { // Rows
            for (int j = 0; j < this.height; j++) { // Columns
                byte byteColor = this.pixelData[j * this.width + i]; // this.pixelData[i + j * this.height]
                int intColor = MapColor.getRenderColor(byteColor);
                this.bufferedMap.setRGB(i, j, bgrToArgb(intColor));
            } // Trust me, I've tried by using a BufferedImage.TYPE_INT_BGR, but this is a workaround.
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
