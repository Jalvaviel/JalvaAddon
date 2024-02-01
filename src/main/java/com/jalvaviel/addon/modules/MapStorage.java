package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import java.util.ArrayList;
import java.util.List;
public class MapStorage extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Debug")
        .defaultValue(false)
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
    public MapStorage() {
        super(Addon.CATEGORY, "Map Storage", "A bot that copies and stores maps in a wall.");
    }

    List<ItemFrameEntity> itemFramesWithMaps = new ArrayList<>();

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if(!itemFramesWithMaps.isEmpty() || mc.player.age % 20 == 0){
            hitNearbyItemFrames(itemFramesWithMaps);
        }
    }

    @Override
    public void onActivate() {
        if(!BaritoneUtils.IS_AVAILABLE){
            ChatUtils.sendMsg(Text.of("Baritone is not installed, please install it to use this module."));
            toggle();
        }else{
            ChatUtils.sendMsg(Text.of("Baritone is installed."));
            /*
              PROCEEDINGS
              1. Create a list with the maps on the item frames: do this with mapdownloader functions.
              2. Baritone pathing near each frame
              3. Baritone hit that frame
              4. Baritone pick the map item
             */
            List<ItemFrameEntity> itemFramesWithMaps = getNearbyMapCoordinates(mapRadius.get());
        }
    }

    private List<ItemFrameEntity> getNearbyMapCoordinates(int boxSize){
        assert mc.player != null;
        Box box = new Box(mc.player.getX()+boxSize,mc.player.getY()+boxSize,mc.player.getZ()+boxSize,mc.player.getX()-boxSize,mc.player.getY()-boxSize,mc.player.getZ()-boxSize);
        assert mc.world != null;
        List<ItemFrameEntity> itemFrameEntities = mc.world.getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class),box, EntityPredicates.VALID_ENTITY);
        List<ItemStack> mapsInItemFrames = new ArrayList<>();
        if(debug.get()){ChatUtils.sendMsg(Text.of("Found "+itemFrameEntities.size()+" item frames."));}
        for(ItemFrameEntity itemFrameEntity : itemFrameEntities) {
            if (itemFrameEntity.getHeldItemStack().getItem() instanceof FilledMapItem) {
                itemFrameEntities.add(itemFrameEntity);
            }
        }
        return itemFrameEntities;
    }

    private void hitNearbyItemFrames(List<ItemFrameEntity> itemFrameEntities){
        for(ItemFrameEntity itemFrameEntity : itemFrameEntities){
            assert mc.player != null;
            if(debug.get()){ChatUtils.sendMsg(Text.of("Trying to attack item frame."));}
            mc.interactionManager.attackEntity(mc.player, itemFrameEntity);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
    /*
    private void pathTowardsItemFrame(ItemFrameEntity itemFrame){
        int x = itemFrame.getBlockX();
        int y = itemFrame.getBlockY();
        int z = itemFrame.getBlockZ();
        Goal goal = new GoalGetToBlock(x, y, z);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoal();
    }
     */
}
