package com.jalvaviel.addon.modules;
import com.jalvaviel.addon.Addon;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import java.util.List;

public class VillagerModule extends Module {
    public VillagerModule() {
        super(Addon.CATEGORY, "VillagerModule", "Search for nearby villagers");
    }

    @Override
    public void onActivate() {
        assert mc.world != null;
        assert mc.player != null;
        getNearbyVillagers(mc.world, mc.player, 200);
    }

    public void getNearbyVillagers(World world, PlayerEntity player, double distance) {
        Box searchArea = new Box(player.getX() - distance, player.getY() - distance, player.getZ() - distance,
            player.getX() + distance, player.getY() + distance, player.getZ() + distance); // Distance to each corner of the box.
        List<VillagerEntity> allVillagers = world.getEntitiesByType(EntityType.VILLAGER, searchArea, null);
        for (VillagerEntity villager : allVillagers) {
            ChatUtils.sendPlayerMsg("Villager found: " + villager.getUuidAsString());
        }
    }
}


