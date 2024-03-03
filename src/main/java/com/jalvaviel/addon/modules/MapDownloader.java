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
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.*;


import static com.jalvaviel.addon.Addon.LOG;
import static com.jalvaviel.addon.utils.CanvasGenerator.getCanvasGenerator;
import static java.lang.Math.abs;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;

public class MapDownloader extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    /*
    private final Setting<String> folderString = sgGeneral.add(new StringSetting.Builder()
        .name("Map folder")
        .description("The folder to store the map images relative to Minecraft's instance directory.")
        .defaultValue("maps")
        .wide()
        .build()
    );
     */

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

    public final Setting<Keybind> saveMapsKeybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("Save Maps Keybind")
        .description("Save Maps by pressing this keybind")
        .defaultValue(Keybind.fromKey(GLFW_KEY_KP_ENTER))
        .build()
    );
    public BlockPos pos1;
    public BlockPos pos2;
    public Box box;
    public int yaw;
    public String folderString = "maps";
    public String axis;
    boolean saveMapsKeybindPressed = false;

    public MapDownloader() {
        super(Addon.CATEGORY, "Map Downloader", "Download maps nearby.");
    }

    @Override
    public void onActivate() {
        boolean isServer = mc.getCurrentServerEntry() != null;
        String folderPath = Utils.getFolderPath(isServer, folderString);
        Utils.createWorldFolder(folderPath);
        if(mapDownloaderMode.get() == MapDownloaderModes.Item_Frames){
            if(pos1 == null || pos2 == null) {
                ChatUtils.sendMsg("JalvaAddons", Text.of("Please select with your wand the corners of your selection"));
            }
        }
    }

    private void saveMaps(String folderPath){
        Map[][] maps;
        if(mapDownloaderMode.get() == MapDownloaderModes.Item_Frames){
            maps = getMapsFromItemFrames();
            CanvasData canvasData = getCanvasGenerator().generateCanvasFromMapMatrix(maps,CanvasType.CUSTOM);
            Utils.writeCanvasToFolder(canvasData,folderPath + "\\" + canvasData.canvasID + ".png");
        }
    }

    private Map[][] getMapsFromItemFrames() { // TODO take into account if the player wants single maps or a canvas
        // NORTH -Z
        // SOUTH +Z
        // EAST +X
        // WEST -X

        axis = box.getLengthX() >= box.getLengthZ() ? "X" : "Z"; // TODO naïve approach
        List<ItemFrameEntity> itemFrameEntities = mc.world.getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class), box, EntityPredicates.VALID_ENTITY);
        int canvasHorizontalLength = (int) (axis == "X" ? box.getLengthX() : box.getLengthZ());
        ChatUtils.sendMsg(Text.of("Entities: "+itemFrameEntities.size()));
        Map[][] canvasMatrix = new Map[canvasHorizontalLength-1][(int) box.getLengthY()-1];

        for(ItemFrameEntity itemFrameEntity : itemFrameEntities){
            int itemFrameHorizontalCoordsWorld = axis == "X" ? itemFrameEntity.getBlockX() : itemFrameEntity.getBlockZ();
            int itemFrameVerticalCoordsWorld = itemFrameEntity.getBlockY();
            int boxHorizontalCorner = (int) (axis == "X" ? box.minX : box.minZ);
            int boxVerticalCorner = (int) box.maxY;

            int indexArrayHorizontal = abs(itemFrameHorizontalCoordsWorld - boxHorizontalCorner);
            int indexArrayVertical = abs(itemFrameVerticalCoordsWorld - boxVerticalCorner);
            //canvasMatrix[indexArrayHorizontal][indexArrayVertical]= new Map(itemFrameEntity.getHeldItemStack());
        }
        return canvasMatrix;
    }

    private void getMapsFromInventory() {
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
    }

    private boolean tryUpdateBox(BlockPos blockPos1, BlockPos blockPos2){
        if (blockPos1 != null && blockPos2 != null && // All the blockpos have to share at least 1 axis (X,Z) and be not null
            (blockPos2.getX()-blockPos1.getX()) * (blockPos2.getZ()-blockPos1.getZ()) == 0) {
                Box tempBox = new Box(blockPos1.toCenterPos(), blockPos2.toCenterPos());
                box = new Box(tempBox.minX-0.5, tempBox.minY-0.5, tempBox.minZ-0.5, tempBox.maxX+0.5, tempBox.maxY+0.5, tempBox.maxZ+0.5);
                return true;
        } else {
            box = null;
        }
        return false;
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
                event.renderer.boxLines(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, Color.RED, 0);
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
                    tryUpdateBox(pos1, pos2);
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
                    tryUpdateBox(pos1, pos2);
                }
            }
        }

        if(saveMapsKeybind.get().isPressed()){ // Only run once is pressed
            if(!saveMapsKeybindPressed){
                saveMaps(folderString);
                ChatUtils.sendMsg("JalvaAddons",Text.of("Saved maps from item frames successfully!"));
                pos1 = null;
                pos2 = null;
                box = null;
                saveMapsKeybindPressed = true;
            }
        } else {
            saveMapsKeybindPressed = false;
        }
    }
}
