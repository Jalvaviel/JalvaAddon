package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.utils.*;
import com.jalvaviel.addon.utils.Map;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.jalvaviel.addon.Addon.LOG;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;

public class MapDownloader extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private enum MapDownloaderModes{
        Player_Inventory,
        Inventories,
        Item_Frames
    }

    private final Setting<MapDownloaderModes> mapDownloaderMode = sgGeneral.add(new EnumSetting.Builder<MapDownloaderModes>()
        .name("Download Mode")
        .description("The mode of map downloading.")
        .defaultValue(MapDownloaderModes.Player_Inventory)
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

    public final Setting<Keybind> setPos1 = sgGeneral.add(new KeybindSetting.Builder()
        .name("First Selection")
        .description("First selection for item frame downloads")
        .defaultValue(Keybind.fromKey(GLFW_KEY_KP_ADD))
        .build()
    );
    public final Setting<Keybind> setPos2 = sgGeneral.add(new KeybindSetting.Builder()
        .name("Second Selection")
        .description("Second selection for item frame downloads")
        .defaultValue(Keybind.fromKey(GLFW_KEY_KP_SUBTRACT))
        .build()
    );
    public BlockPos pos1;
    public BlockPos pos2;
    public Box box;
    public int yaw;
    public String folderString = "maps";

    public MapDownloader() {
        super(Addon.CATEGORY, "Map Downloader", "Download maps nearby.");
    }

    @Override
    public void onActivate() {
        boolean isServer = mc.getCurrentServerEntry() != null;
        String folderPath = getFolderPath(isServer);
        createWorldFolder(folderPath);
        if(mapDownloaderMode.get() == MapDownloaderModes.Inventories){
            saveMaps(folderPath);
            ChatUtils.sendMsg("JalvaAddons",Text.of("Saved maps from inventory successfully!"));
            toggle();
        }
        if(mapDownloaderMode.get() == MapDownloaderModes.Item_Frames){
            if(pos1 == null || pos2 == null){
                ChatUtils.sendMsg("JalvaAddons",Text.of("Please select with your wand the corners of your selection"));
            } else {
                box = new Box(pos1,pos2);
                saveMaps(folderPath);
                ChatUtils.sendMsg("JalvaAddons",Text.of("Saved maps from item frames successfully!"));
                pos1 = null;
                pos2 = null;
                toggle();
            }
        }
    }


    private void saveMaps(String folderPath){
        ArrayList<Map> maps = null;
        if(mapDownloaderMode.get() == MapDownloaderModes.Item_Frames){
            // TODO maps = getMapsFromItemFrames();
        }
        if (mapDownloaderMode.get() == MapDownloaderModes.Player_Inventory) {
            maps = getMapsFromInventory();
        }
        if (saveMapsAsCanvas.get()) {
            if (mapBackground.get()) {
                FramedCanvas canvas = new FramedCanvas(maps, Canvas.CanvasType.PLAYER_INVENTORY);
                writeCanvasToFolder(canvas, folderPath + "\\" + canvas.canvasID + ".png");
            } else {
                Canvas canvas = new Canvas(maps, Canvas.CanvasType.PLAYER_INVENTORY);
                writeCanvasToFolder(canvas, folderPath + "\\" + canvas.canvasID + ".png");
            }
        } else {
            for (Map map : maps) {
                writeMapToFolder(map, folderPath + "\\" + map.imageID + ".png");
            }
        }
    }

    private ArrayList<Map> getMapsFromInventory(){
        PlayerInventory playerInventory = mc.player.getInventory();
        ArrayList<Map> mapsInInventory = new ArrayList<>();
        for (ItemStack itemStack : playerInventory.main.subList(9, 36)) {
            if(itemStack.getItem() instanceof FilledMapItem) {
                if (mapBackground.get() && !saveMapsAsCanvas.get()) {
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
        // This is done to flip the first 9 slots of the inventory from the top part to the bottom in case there's a canvas.
        for (ItemStack itemStack : playerInventory.main.subList(0, 9)) {
            if(itemStack.getItem() instanceof FilledMapItem) {
                if (mapBackground.get() && !saveMapsAsCanvas.get()) {
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
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString + "\\" + Objects.requireNonNull(mc.getCurrentServerEntry()).name;
        }else{
            return FabricLoader.getInstance().getGameDir() + "\\" + folderString + "\\" + Objects.requireNonNull(mc.getServer()).getSaveProperties().getLevelName();
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
            if(map.imageID.toString() != "DEADBEEF-0000-0000-0000-000000000000"){ // TODO clean this mess
                ImageIO.write(map.bufferedMap, "png", output);
            }
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

    /**
     * Wand selection
     **/

    @EventHandler
    private void onRender(Render3DEvent event) {
        try {
            int x1 = pos1.getX();
            int y1 = pos1.getY();
            int z1 = pos1.getZ();
            int x2 = pos2.getX();
            int y2 = pos2.getY();
            int z2 = pos2.getZ();
            if(pos1.equals(pos2)){
                event.renderer.blockLines(x1,y1,z1, Color.YELLOW,0);
            } else {
                event.renderer.blockLines(x1,y1,z1, Color.MAGENTA,0);
                event.renderer.blockLines(x2,y2,z2, Color.CYAN,0);
            }
        } catch (Exception e) {
            LOG.warn("Couldn't render the block outlines in the map selection");
        }
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event){
        if(setPos1.get().isPressed()){
            HitResult hitResult = mc.crosshairTarget;
            if(hitResult != null && hitResult.getType() == HitResult.Type.ENTITY){
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity instanceof ItemFrameEntity) {
                    yaw = (int) entity.getYaw();
                    pos1 = entity.getBlockPos();
                }
            }
        }
        if(setPos2.get().isPressed()){
            HitResult hitResult = mc.crosshairTarget;
            if(hitResult != null && hitResult.getType() == HitResult.Type.ENTITY){
                Entity entity = ((EntityHitResult) hitResult).getEntity();
                if (entity instanceof ItemFrameEntity) {
                    yaw = (int) entity.getYaw();
                    pos2 = entity.getBlockPos();
                }
            }
        }
    }
}
