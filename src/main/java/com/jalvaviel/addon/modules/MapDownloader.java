package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.utils.FramedMap;
import com.jalvaviel.addon.utils.Map;
import com.jalvaviel.addon.utils.Canvas;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

import static com.jalvaviel.addon.Addon.LOG;

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

    private final Setting<Boolean> saveMapsAsCanvas = sgGeneral.add(new BoolSetting.Builder()
        .name("Save maps in a canvas.")
        .description("Saves multiple maps in the same image.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> mapBackground = sgGeneral.add(new BoolSetting.Builder()
        .name("Map background")
        .description("Saves the images with a map texture background.") // Requested by Badinek
        .defaultValue(false)
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
            saveMapsFromInventory(folderPath);
            ChatUtils.sendMsg("JalvaAddons",Text.of("Saved maps from inventory successfully!"));
        }
        if(saveMapsFromEntity.get()){
            // TODO
        }
        toggle();
    }

    private void saveMapsFromInventory(String folderPath){
        ArrayList<Map> mapsInInventory = getMapsFromInventory();
        if(saveMapsAsCanvas.get()){
            Canvas canvas = new Canvas(mapsInInventory, Canvas.CanvasType.PLAYER_INVENTORY);
            writeCanvasToFolder(canvas, folderPath + "\\" + canvas.canvasID + ".png");
        }
        for(Map map : mapsInInventory){
            writeMapToFolder(map,folderPath + "\\" + map.imageID + ".png");
        }
    }

    private ArrayList<Map> getMapsFromInventory(){
        PlayerInventory playerInventory = mc.player.getInventory();
        ArrayList<Map> mapsInInventory = new ArrayList<>();
        for (ItemStack itemStack : playerInventory.main) {
            if(itemStack.getItem() instanceof FilledMapItem) {
                if (mapBackground.get()) {
                    FramedMap map = new FramedMap(itemStack);
                    mapsInInventory.add(map);
                } else {
                    Map map = new Map(itemStack);
                    mapsInInventory.add(map);
                }
            } else {
                Map map = new Map(true);
                mapsInInventory.add(map);
            }
        }
        return mapsInInventory;
    }


    /**
     * Folder and .png creation
     **/

    private @NotNull String getFolderPath(boolean isServer){
        if(isServer) {
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString.get() + "\\" + Objects.requireNonNull(mc.getCurrentServerEntry()).name;
        }else{
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString.get() + "\\" + Objects.requireNonNull(mc.getServer()).getSaveProperties().getLevelName();
        }
    }

    private void createWorldFolder(String folderPath) {
        File mapImagesFolder = new File(folderPath);
        if (!mapImagesFolder.exists()) {
            if(!mapImagesFolder.mkdirs()) {
                LOG.warn("Couldn't create the map folder, maybe it does exist already?");
            };
        }
    }

    private void writeMapToFolder(Map map, String fullPath){
        try {
            File output = new File(fullPath);
            ImageIO.write(map.bufferedMap, "png", output);
        } catch (IOException e) {
            LOG.warn("Couldn't store the map "+ map.imageID.toString());
        }
    }

    private void writeCanvasToFolder(Canvas canvas, String fullPath){
        try {
            File output = new File(fullPath);
            ImageIO.write(canvas.bufferedCanvas, "png", output);
        } catch (IOException e) {
            LOG.warn("Couldn't store the map "+ canvas.canvasID.toString());
        }
    }
}
