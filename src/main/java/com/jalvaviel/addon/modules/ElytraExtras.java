package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.resource.metadata.AnimationFrameResourceMetadata;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.*;
import static net.fabricmc.loader.impl.util.log.Log.log;

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
        .description("The delay before using another firework in ticks.")
        .defaultValue(10)
        .sliderRange(0,100)
        .visible(doUseFireworks::get)
        .build()
    );

    public final Setting<Boolean> antiAfk = sgMisc.add(new BoolSetting.Builder()
        .name("anti-afk")
        .description("Swings the hand to prevent getting kicked out.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> superSecretSetting = sgMisc.add(new BoolSetting.Builder()
        .name("super-secret-setting")
        .description("test")
        .defaultValue(false)
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
                    //mc.player.startFallFlying();
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
            if (currentPlayerSpeed <= fireworkMinSpeed.get()) fireworkCounter++;
            FindItemResult fireworks = InvUtils.find(Items.FIREWORK_ROCKET);
            if (fireworks.found() && fireworkCounter >= fireworkDelay.get() && recastCheck()) {
                InvUtils.move().from(fireworks.slot()).toHotbar(replenishSlot.get()-1);
                InvUtils.swap(replenishSlot.get()-1, true);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                InvUtils.swapBack();
                fireworkCounter = 0;
            }
        }
    }

    private void antiAfk() {
        if (antiAfk.get() && mc.player.getAbilities().flying && mc.player.age % 120 == 0) {
            mc.player.swingHand(mc.player.getActiveHand());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        double dx = mc.player.getX() - mc.player.prevX;
        double dy = mc.player.getY() - mc.player.prevY;
        double dz = mc.player.getZ() - mc.player.prevZ;
        currentPlayerSpeed = Math.sqrt(dx*dx + dy*dy + dz*dz) * 20;
        replaceElytra();
        recast();
        fixYaw();
        useFireworks();
        antiAfk();
    }

    private boolean recastCheck() {
        ItemStack itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        //info(String.valueOf(mc.player.getAbilities()));
        return (!mc.player.getAbilities().flying && mc.player.getY() - mc.player.prevY < 0 && !mc.player.isSwimming() && !mc.player.hasVehicle() && !mc.player.isClimbing() && itemStack.isOf(Items.ELYTRA) && !itemStack.willBreakNextUse());
    }

    Identifier texture;
    Identifier sprite;
    SpriteContents spriteContents;
    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (superSecretSetting.get()) {
            int frameOffset = (int) (event.tickDelta * 10);
            event.drawContext.drawTexture(RenderLayer::getGuiTextured, texture, 256, 256 * 10, 0, 256 * frameOffset, 0, 0, 256, 256);
        }
    }

    private void initSuperSecretSetting() throws IOException {
        texture = Identifier.of("jalvaaddon","textures/super_secret_setting.png");
        sprite = Identifier.of("jalvaaddon","super_secret_contents");
        Resource resource = mc.getResourceManager().getResource(texture).get();
        NativeImage nativeImage = NativeImage.read(resource.getInputStream());
        List<AnimationFrameResourceMetadata> frames = new ArrayList<>();
        spriteContents = new SpriteContents(texture
            , new SpriteDimensions(256,256), nativeImage
            , new ResourceMetadata.Builder().add(AnimationResourceMetadata.SERIALIZER,
                new AnimationResourceMetadata(Optional.of(frames), Optional.of(256), Optional.of(256),1,false)).build());
    }

    @Override
    public void onActivate() {
        if (superSecretSetting.get()) {
            try {
                initSuperSecretSetting();
            } catch (IOException ignored) {}
        }
    }
}
