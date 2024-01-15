package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.*;

public class MapDownloader extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> folderString = sgGeneral.add(new StringSetting.Builder()
        .name("Map folder")
        .description("The folder to store the map images.")
        .defaultValue("maps")
        .wide()
        .build()
    );

    private final Setting<Integer> mapRadius = sgGeneral.add(new IntSetting.Builder()
        .name("Save maps in radius")
        .description("Set the radius around the player where maps will be downloaded")
        .defaultValue(16)
        .min(1)
        .sliderMax(64)
        .build()
    );

    private final Setting<Boolean> saveMapsFromEntity = sgGeneral.add(new BoolSetting.Builder()
        .name("Save maps from entity")
        .description("Saves maps in item frames.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> saveMapsFromInventory = sgGeneral.add(new BoolSetting.Builder()
        .name("Save maps from inventory")
        .description("Saves maps in the player's inventory.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Debug")
        .defaultValue(true)
        .build()
    );

    public MapDownloader() {
        super(Addon.CATEGORY, "Map Downloader", "Download maps nearby.");
    }

    @Override
    public void onActivate() {
        boolean isServer = mc.getCurrentServerEntry() != null;
        String folderPath = getFolderPath(isServer);
        createWorldFolder(folderPath);
        if(saveMapsFromInventory.get()){
            List<ItemStack> mapsInInventory = getMapsInInventory(mc.player.getInventory());
            for(ItemStack map : mapsInInventory){
                try {
                    byte[] pixelData = getPixelDataFromMap(map);
                    BufferedImage img = convertMap(pixelData);
                    String filename = generateImageIdentifier(pixelData).toString();
                    writeImageToFolder(img,folderPath+'\\'+filename+".png");
                } catch (NullPointerException e) {
                    if (debug.get()) {ChatUtils.sendMsg(Text.of("An exception has occurred, stopping."));}
                }
            }
        }
        if(saveMapsFromEntity.get()){
            List<ItemStack> mapsInItemFrames = getMapsInItemFrames(mapRadius.get());
            for(ItemStack map : mapsInItemFrames){
                try {
                    byte[] pixelData = getPixelDataFromMap(map);
                    BufferedImage img = convertMap(pixelData);
                    String filename = generateImageIdentifier(pixelData).toString();
                    writeImageToFolder(img,folderPath+'\\'+filename+".png");
                } catch (NullPointerException e) {
                    if (debug.get()) {ChatUtils.sendMsg(Text.of("An exception has occurred, stopping."));}
                }
            }
        }
    }

    private @NotNull List<ItemStack> getMapsInItemFrames(int boxSize){
        Box box = new Box(mc.player.getX()+boxSize,mc.player.getY()+boxSize,mc.player.getZ()+boxSize,mc.player.getX()-boxSize,mc.player.getY()-boxSize,mc.player.getZ()-boxSize);
        List<ItemFrameEntity> itemFrameEntities = mc.world.getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class),box, EntityPredicates.VALID_ENTITY);
        List<ItemStack> mapsInItemFrames = new ArrayList<>();
        if(debug.get()){ChatUtils.sendMsg(Text.of("Found "+itemFrameEntities.size()+" item frames."));}
        for(ItemFrameEntity itemFrameEntity : itemFrameEntities){
            mapsInItemFrames.add(itemFrameEntity.getHeldItemStack());
        }
        return mapsInItemFrames;
    }

    private @NotNull String getFolderPath(boolean isServer){
        if(isServer) {
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString.get() + "\\" + mc.getCurrentServerEntry().name;
        }
        else{
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString.get() + "\\" + mc.getServer().getSaveProperties().getLevelName();
        }
    }

    private void createWorldFolder(String folderPath) {
        File mapImagesFolder = new File(folderPath);
        if (!mapImagesFolder.exists()) {
            if (mapImagesFolder.mkdirs()) {
                if (debug.get()) {ChatUtils.sendMsg(Text.of("Created directory: " + mapImagesFolder));}
            } else {
                if (debug.get()) {ChatUtils.sendMsg(Text.of("Failed to create directory: " + mapImagesFolder));}
            }
        }
    }

    private @NotNull List<ItemStack> getMapsInInventory(@NotNull PlayerInventory playerInventory) {
        List<ItemStack> mapsInInventory = new ArrayList<>();
        for (ItemStack itemStack : playerInventory.main) {
            if (itemStack.getItem() instanceof FilledMapItem) {
                mapsInInventory.add(itemStack);
            }
        }
        return mapsInInventory;
    }

    private byte @Nullable [] getPixelDataFromMap(ItemStack itemStack) {
        MapState mapState = FilledMapItem.getMapState(itemStack, mc.world);
        // Get the pixels of the map in its MapState.

        if (mapState != null && mapState.colors != null && mapState.colors.length == 16384) {
            // Ensure mapState is not null and has the expected data size (128x128)
            return mapState.colors;
        } else {
            if (debug.get()) {ChatUtils.sendMsg(Text.of("Couldn't retrieve the pixel data from the map."));}
            return null;
        }
    }

    private @NotNull BufferedImage convertMap(byte[] pixelData) {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 16384; i++) {
            byte byteColor = pixelData[i];
            int intColor = MapColor.getRenderColor(byteColor);
            img.setRGB(i % 128, i / 128, bgrToRgb(intColor)); //for some ducking reason, it's in bgr and not rgb
        }
        return img;
    }

    private int bgrToRgb(int bgrColor) {
        int blue = (bgrColor >> 16) & 0xFF;  // Extract the blue channel
        int green = (bgrColor >> 8) & 0xFF;  // Extract the green channel
        int red = bgrColor & 0xFF;           // Extract the red channel
        return (red << 16) | (green << 8) | blue;
    }

    @Contract("_ -> new")
    private @NotNull UUID generateImageIdentifier(byte[] pixelData){
        return UUID.nameUUIDFromBytes(pixelData);
    }

    private void writeImageToFolder(BufferedImage img, String fullPath){
        try {
            File output = new File(fullPath);
            ImageIO.write(img, "png", output);
            if (debug.get()) {ChatUtils.sendMsg(Text.of("Image saved to: " + output));}
        } catch (IOException e) {
            if (debug.get()) {ChatUtils.sendMsg(Text.of("Couldn't write file to path."));}
        }
    }
}
