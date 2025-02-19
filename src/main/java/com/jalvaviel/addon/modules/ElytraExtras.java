package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.text.CustomTextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import meteordevelopment.meteorclient.systems.config.Config;

public class ElytraExtras extends Module {
    public SettingGroup sgMisc = settings.getDefaultGroup();
    public SettingGroup sgRender = settings.createGroup("Render");
    private double currentPlayerSpeed;
    private int elytraCounter;
    private int fireworkCounter;

    public ElytraExtras() {
        super(Addon.CATEGORY, "elytra-extras", "Extra settings for your elytra.");
    }

    public final Setting<Boolean> doReplaceElytra = sgMisc.add(new BoolSetting.Builder()
        .name("replace-elytra")
        .description("Replaces elytra when damaged.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> elytraDamage = sgMisc.add(new IntSetting.Builder()
        .name("elytra-damage")
        .description("The damage threshold to swap the elytra")
        .defaultValue(10)
        .sliderRange(1,431)
        .visible(doReplaceElytra::get)
        .build()
    );

    public final Setting<Boolean> fixYaw = sgMisc.add(new BoolSetting.Builder()
        .name("fix-yaw")
        .description("Fixes your yaw in 45 degree angles.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> doRecast = sgMisc.add(new BoolSetting.Builder()
        .name("auto-recast")
        .description("Recasts elytra if they aren't open.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> recastDelay = sgMisc.add(new IntSetting.Builder()
        .name("recast-delay")
        .description("The delay before recasting in ticks.")
        .defaultValue(10)
        .sliderRange(0,100)
        .visible(doRecast::get)
        .build()
    );

    public final Setting<Boolean> doUseFireworks = sgMisc.add(new BoolSetting.Builder()
        .name("use-fireworks")
        .description("Uses fireworks to boost you when you slow down.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> replenishSlot = sgMisc.add(new IntSetting.Builder()
        .name("replenish-slot")
        .description("The hotbar slot to move the fireworks.")
        .defaultValue(9)
        .sliderRange(1,9)
        .visible(doUseFireworks::get)
        .build()
    );

    public final Setting<Double> fireworkMinSpeed = sgMisc.add(new DoubleSetting.Builder()
        .name("firework-min-speed")
        .description("The minimum speed before using a firework.")
        .defaultValue(20)
        .sliderRange(0,40)
        .visible(doUseFireworks::get)
        .build()
    );

    public final Setting<Integer> fireworkDelay = sgMisc.add(new IntSetting.Builder()
        .name("firework-delay")
        .description("The delay before using another firework.")
        .defaultValue(10)
        .sliderRange(0,100)
        .visible(doUseFireworks::get)
        .build()
    );

    public final Setting<Boolean> speedometer = sgRender.add(new BoolSetting.Builder()
        .name("speedometer")
        .description("Displays a speedometer.")
        .defaultValue(true)
        .build()
    );

    public final Setting<SettingColor> speedColor = sgRender.add(new ColorSetting.Builder()
        .name("Speed Color")
        .description("Color for the speedometer.")
        .defaultValue(Color.WHITE)
        .build()
    );

    private void replaceElytra() {
        if (doReplaceElytra.get()) {
            ItemStack chestStack = mc.player.getInventory().getArmorStack(2);
            if (chestStack.getItem() == Items.ELYTRA) {
                if (chestStack.getMaxDamage() - chestStack.getDamage() <= elytraDamage.get()) {
                    FindItemResult elytra = InvUtils.find(stack -> stack.getMaxDamage() - stack.getDamage() > elytraDamage.get() && stack.getItem() == Items.ELYTRA);
                    InvUtils.move().from(elytra.slot()).toArmor(2);
                }
            }
        }
    }

    private void recast() {
        if (doRecast.get()) {
            if (recastCheck() && !mc.player.isOnGround()) {
                elytraCounter++;
                if (elytraCounter >= recastDelay.get()) {
                    mc.player.startFallFlying();
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    elytraCounter = 0;
                }
            }
        }
    }

    private void fixYaw() {
        if (fixYaw.get()) {
            float nearestYaw = Math.round((mc.player.getYaw() + 1f) / 45f) * 45f;
            mc.player.setYaw((float) MathHelper.lerp(0.3,mc.player.getYaw(),nearestYaw));
        }
    }

    private void useFireworks() {
        if (doUseFireworks.get()) {
            fireworkCounter++;
            FindItemResult fireworks = InvUtils.find(Items.FIREWORK_ROCKET);
            if (fireworks.found() && fireworkCounter >= fireworkDelay.get() && currentPlayerSpeed <= fireworkMinSpeed.get() && !recastCheck()) {
                InvUtils.move().from(fireworks.slot()).toHotbar(replenishSlot.get()-1);
                InvUtils.swap(replenishSlot.get()-1, true);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                InvUtils.swapBack();
                fireworkCounter = 0;
            }
        }
    }
    @EventHandler
    private void onTick(TickEvent.Pre event){
        if (!this.isActive()) return;
        assert mc.player != null;
        double dx = mc.player.getX() - mc.player.prevX;
        double dy = mc.player.getY() - mc.player.prevY;
        double dz = mc.player.getZ() - mc.player.prevZ;
        currentPlayerSpeed = Math.sqrt(dx*dx + dy*dy + dz*dz) * 20;
        replaceElytra();
        recast();
        fixYaw();
        useFireworks();
    }

    private boolean recastCheck() {
        ItemStack itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return (!mc.player.isFallFlying() && !mc.player.hasVehicle() && !mc.player.isClimbing() && itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack));
    }

    CustomTextRenderer speedRenderer = new CustomTextRenderer(Config.get().font.get());

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (speedometer.get() && this.isActive()) {
            String speed = String.format("Speed: %.3f b/s",currentPlayerSpeed);
            double dx =  (mc.getWindow().getFramebufferWidth()-speedRenderer.getWidth(speed))/2;
            double paddingDy = mc.player.getArmor() > 0 ? 66.0f : 55.0f;
            double dy = mc.getWindow().getFramebufferHeight()-(paddingDy*mc.getWindow().getScaleFactor());
            speedRenderer.render(speed, dx, dy, speedColor.get(), false);
        }
    }
}
