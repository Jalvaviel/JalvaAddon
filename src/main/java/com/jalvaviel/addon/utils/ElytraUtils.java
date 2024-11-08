package com.jalvaviel.addon.utils;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce.checkConditions;
import static meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce.recastElytra;

public final class ElytraUtils {

    private static ElytraUtils instance;
    private static boolean doReplaceElytra;
    private static int elytraReplaceDamage;
    private static boolean doUseFireworks;
    private static double minimumFireworkSpeed;
    private static boolean doReplenishFireworks;
    private static int replenishFireworkSlot;
    private static boolean autoRecast;

    private ElytraUtils() {}

    public static ElytraUtils getInstance() {
        if (instance == null) {
            instance = new ElytraUtils();
            buildDefaults();
        }
        return instance;
    }

    private static void buildDefaults() {
        doReplaceElytra = false;
        elytraReplaceDamage = 10;
        doUseFireworks = false;
        minimumFireworkSpeed = 1;
        doReplenishFireworks = false;
        replenishFireworkSlot = 8;
        autoRecast = false;
    }

    public static void onTick(){
        assert mc.player != null;
        assert mc.interactionManager != null;
        if (doReplaceElytra) {
            ItemStack chestStack = mc.player.getInventory().getArmorStack(2);
            if (chestStack.getItem() == Items.ELYTRA) {
                if (chestStack.getMaxDamage() - chestStack.getDamage() <= elytraReplaceDamage) {
                    FindItemResult elytra = InvUtils.find(stack -> stack.getMaxDamage() - stack.getDamage() > elytraReplaceDamage && stack.getItem() == Items.ELYTRA);
                    InvUtils.move().from(elytra.slot()).toArmor(2);
                }
            }
        }

        if (doReplenishFireworks) {
            FindItemResult fireworks = InvUtils.find(Items.FIREWORK_ROCKET);

            if (fireworks.found() && !fireworks.isHotbar()) {
                InvUtils.move().from(fireworks.slot()).toHotbar(replenishFireworkSlot);
            }
        }

        if (doUseFireworks) {
            FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
            if (!itemResult.found()) return;

            if (itemResult.isOffhand()) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                mc.player.swingHand(Hand.OFF_HAND);
            } else {
                InvUtils.swap(itemResult.slot(), true);

                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);

                InvUtils.swapBack();
            }
        }

        if (autoRecast) {
            recastElytra(mc.player);
        }
    }

    /** GETTER & SETTER **/

    public static boolean isDoReplaceElytra() {
        return doReplaceElytra;
    }

    public static void setDoReplaceElytra(boolean doReplaceElytra) {
        ElytraUtils.doReplaceElytra = doReplaceElytra;
    }

    public static boolean isDoUseFireworks() {
        return doUseFireworks;
    }

    public static void setDoUseFireworks(boolean doUseFireworks) {
        ElytraUtils.doUseFireworks = doUseFireworks;
    }

    public static double getMinimumFireworkSpeed() {
        return minimumFireworkSpeed;
    }

    public static void setMinimumFireworkSpeed(double minimumFireworkSpeed) {
        ElytraUtils.minimumFireworkSpeed = minimumFireworkSpeed;
    }

    public static boolean isDoReplenishFireworks() {
        return doReplenishFireworks;
    }

    public static void setDoReplenishFireworks(boolean doReplenishFireworks) {
        ElytraUtils.doReplenishFireworks = doReplenishFireworks;
    }

    public static int getReplenishFireworkSlot() {
        return replenishFireworkSlot;
    }

    public static void setReplenishFireworkSlot(int replenishFireworkSlot) {
        ElytraUtils.replenishFireworkSlot = replenishFireworkSlot;
    }

    public static boolean isAutoRecast() {
        return autoRecast;
    }

    public static void setAutoRecast(boolean autoRecast) {
        ElytraUtils.autoRecast = autoRecast;
    }

    public static int getElytraReplaceDamage() {
        return elytraReplaceDamage;
    }

    public static void setElytraReplaceDamage(int elytraReplaceDamage) {
        ElytraUtils.elytraReplaceDamage = elytraReplaceDamage;
    }
}
