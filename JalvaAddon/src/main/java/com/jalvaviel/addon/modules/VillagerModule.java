package com.jalvaviel.addon.modules;
import com.jalvaviel.addon.Addon;

import com.jalvaviel.addon.auxiliary.AbstractTradingScreenHandler;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;
import java.util.function.Predicate;

/*
for (VillagerEntity villager : allVillagers) {

    String uuidAsString = villager.getUuidAsString();
    ChatUtils.sendMsg(Text.of("UUID: " + uuidAsString + " | " + " Position: " + villager.getX() + "," + villager.getZ()));
}
*/

public class VillagerModule extends Module {
    public VillagerEntity villager;
    public VillagerModule() {
        super(Addon.CATEGORY, "VillagerModule", "Search for nearby villagers");
    }

    @Override
    public void onActivate() {
        assert mc.world != null;
        assert mc.player != null;
        List<VillagerEntity> allVillagers = getNearbyVillagers(mc.world, mc.player, 4);
        this.villager = allVillagers.get(0);
    }

    public List<VillagerEntity> getNearbyVillagers(World world, ClientPlayerEntity player, double distance) {
        Box searchArea = new Box(player.getX() - distance, player.getY() - distance, player.getZ() - distance,
            player.getX() + distance, player.getY() + distance, player.getZ() + distance); // Distance to each corner of the box.
        Predicate<Entity> entityPredicate = entity -> entity instanceof VillagerEntity
            && searchArea.contains(entity.getPos());
        return world.getEntitiesByType(EntityType.VILLAGER, searchArea, entityPredicate);
    }

    public void tick(AbstractTradingScreenHandler c){
        if (mc.player.age % 10 == 0) return;
        takeResults(c);
        insertItems(c);
    }

    private void takeResults(AbstractTradingScreenHandler c) {
        ItemStack resultStack = c.slots.get(2).getStack();
        if (resultStack.isEmpty()) return;

        InvUtils.quickMove().slotId(2);

        if (!resultStack.isEmpty()) {
            error("Your inventory is full. Disabling.");
            toggle();
        }
    }

    private void insertItems(AbstractTradingScreenHandler c) {
        ItemStack inputItemStack = c.slots.get(0).getStack();
        if (!inputItemStack.isEmpty()) return;
        int slot = -1;
        for (int i = 3; i < c.slots.size(); i++) {
            ItemStack item = c.slots.get(i).getStack();
            if (item.getItem() != Items.EMERALD) continue;
            slot = i;
            break;
        }

        if (slot == -1) {
            error("You do not have any items in your inventory that can be traded. Disabling.");
            toggle();
            return;
        }

        InvUtils.move().fromId(slot).toId(0);
    }
}



