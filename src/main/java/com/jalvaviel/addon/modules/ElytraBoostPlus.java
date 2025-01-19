package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;


public class ElytraBoostPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMisc = settings.createGroup("Miscellaneous");

    public ElytraBoostPlus() {
        super(Addon.CATEGORY, "elytra-boost-+", "Borrowed from ThunderClient");
    }

    public final Setting<Boolean> onlySpace = sgGeneral.add(new BoolSetting.Builder()
        .name("only-space")
        .description("Boost yourself by pressing the spacebar only.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> cruiseControl = sgGeneral.add(new BoolSetting.Builder()
        .name("cruise-control")
        .description("Use the autopilot to level yourself.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> minUpSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-up-speed")
        .description("Minimum upwards speed before stabilizing.")
        .min(0.1f)
        .max(5.0f)
        .defaultValue(0.5f)
        .visible(cruiseControl::get)
        .build()
    );

    public final Setting<Boolean> forceHeight = sgGeneral.add(new BoolSetting.Builder()
        .name("force-height")
        .description("Force the player to level at a certain height.")
        .defaultValue(false)
        .visible(cruiseControl::get)
        .build()
    );

    public final Setting<Integer> manualHeight = sgGeneral.add(new IntSetting.Builder()
        .name("manual-height")
        .description("The Y coordinate to level height.")
        .defaultValue(121)
        .sliderRange(1,256)
        .visible(() -> forceHeight.get() && cruiseControl.get())
        .build()
    );

    public final Setting<Double> factor = sgGeneral.add(new DoubleSetting.Builder()
        .name("factor")
        .description("Acceleration factor.")
        .min(0.1f)
        .max(50.0f)
        .defaultValue(1.5f)
        .build()
    );

    public final Setting<Boolean> speedLimit = sgGeneral.add(new BoolSetting.Builder()
        .name("speed-limit")
        .description("Sets a speed limit.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> maxSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-speed")
        .description("The speed limit in blocks per tick.")
        .min(0.1f)
        .max(510.0f)
        .defaultValue(2.5f)
        .build()
    );

    /** MISC SETTINGS **/

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

    public final Setting<Boolean> doReplenishFireworks = sgMisc.add(new BoolSetting.Builder()
        .name("replenish-fireworks")
        .description("Replenishes fireworks from the player's inventory.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> replenishSlot = sgMisc.add(new IntSetting.Builder()
        .name("replenish-slot")
        .description("The hotbar slot to move the fireworks.")
        .defaultValue(9)
        .sliderRange(1,9)
        .visible(doReplenishFireworks::get)
        .build()
    );

    public final Setting<Boolean> doUseFireworks = sgMisc.add(new BoolSetting.Builder()
        .name("use-fireworks")
        .description("Uses fireworks to boost you when you slow down.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> fireworkMinSpeed = sgMisc.add(new DoubleSetting.Builder()
        .name("firework-min-speed")
        .description("The minimum speed before using a firework.")
        .defaultValue(1)
        .sliderRange(0,2.90)
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

    public final Setting<Boolean> fixYaw = sgMisc.add(new BoolSetting.Builder()
        .name("fix-yaw")
        .description("Fixes your yaw in 45 degree angles.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> avoidObstacles = sgMisc.add(new BoolSetting.Builder()
        .name("avoid-obstacles")
        .description("Avoids obstacles in nether highways.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> obstacleDistance = sgMisc.add(new IntSetting.Builder()
        .name("obstacle-distance")
        .description("The max distance to check the obstacle.")
        .defaultValue(20)
        .sliderRange(1,64)
        .visible(avoidObstacles::get)
        .build()
    );

    public final Setting<Double> deltaHorizontal = sgMisc.add(new DoubleSetting.Builder()
        .name("delta-horizontal")
        .description("The horizontal offset to take into account for the player hitbox.")
        .defaultValue(0.6)
        .sliderRange(0,4)
        .visible(avoidObstacles::get)
        .build()
    );

    public final Setting<Double> deltaVertical = sgMisc.add(new DoubleSetting.Builder()
        .name("delta-vertical")
        .description("The vertical offset to take into account for the player hitbox.")
        .defaultValue(2)
        .sliderRange(0,4)
        .visible(avoidObstacles::get)
        .build()
    );


    protected float currentPlayerSpeed;
    protected float height;
    private int elytraCounter;
    private int fireworkCounter;

    private double[] forwardWithoutStrafe(final double d) {
        assert mc.player != null;
        float f3 = mc.player.getYaw();
        final double d4 = d * Math.cos(Math.toRadians(f3 + 90.0f));
        final double d5 = d * Math.sin(Math.toRadians(f3 + 90.0f));
        return new double[]{d4, d5};
    }

    private void onAvoidObstacles() {
        assert mc.player != null;
        Vec3d playerPos = mc.player.getPos();
        Vec3d direction = mc.player.getRotationVector();
        direction = new Vec3d(direction.x, 0, direction.z).normalize(); // Flatten to horizontal direction
        Vec3d start = playerPos;
        Vec3d end = playerPos.add(direction.multiply(obstacleDistance.get()));
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        BlockHitResult hitResult = mc.world.raycast(context);
        if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
            Vec3d hitPos = Vec3d.ofCenter(hitResult.getBlockPos()); // Center of the block
            double rawHorizontalDistance = hitPos.distanceTo(playerPos);
            double adjustedHorizontalDistance = rawHorizontalDistance - 0.6; // 0.6 / 2 since we only adjust one side
            adjustedHorizontalDistance = Math.max(adjustedHorizontalDistance, 0);
            double heightDifference = hitPos.y - (playerPos.y + 2); // Approx. player eye height above feet (1.62 blocks)
            float pitch = (float) Math.toDegrees(Math.atan2(heightDifference, adjustedHorizontalDistance));
            mc.player.setPitch(pitch);
        }
    }

    @Override
    public void onActivate(){
        assert mc.player != null;
        height = (float) mc.player.getY();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event){
        assert mc.player != null;
        currentPlayerSpeed = (float) Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);

        if (doReplaceElytra.get()) {
            ItemStack chestStack = mc.player.getInventory().getArmorStack(2);
            if (chestStack.getItem() == Items.ELYTRA) {
                if (chestStack.getMaxDamage() - chestStack.getDamage() <= elytraDamage.get()) {
                    FindItemResult elytra = InvUtils.find(stack -> stack.getMaxDamage() - stack.getDamage() > elytraDamage.get() && stack.getItem() == Items.ELYTRA);
                    InvUtils.move().from(elytra.slot()).toArmor(2);
                }
            }
        }

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

        if (fixYaw.get()) {
            float nearestYaw = Math.round((mc.player.getYaw() + 1f) / 45f) * 45f;
            mc.player.setYaw((float) MathHelper.lerp(0.3,mc.player.getYaw(),nearestYaw));
        }

        if (avoidObstacles.get()) {
            onAvoidObstacles();
        }

        if (doReplenishFireworks.get()) {
            FindItemResult fireworks = InvUtils.find(Items.FIREWORK_ROCKET);

            if (fireworks.found() && !fireworks.isHotbar()) {
                InvUtils.move().from(fireworks.slot()).toHotbar(replenishSlot.get()-1);
            }
        }

        if (doUseFireworks.get()) {
            fireworkCounter++;
            FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
            if (!itemResult.found() || mc.player.getMovement().lengthSquared() >= fireworkMinSpeed.get()) return;
            if (fireworkCounter >= fireworkDelay.get()) {
                if (itemResult.isOffhand()) {
                    mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                    mc.player.swingHand(Hand.OFF_HAND);
                } else {
                    InvUtils.swap(itemResult.slot(), true);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    InvUtils.swapBack();
                }
                fireworkCounter = 0;
            }
        }

    }

    private boolean recastCheck() {
        assert mc.player != null;
        ItemStack itemStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return (!mc.player.isFallFlying() && !mc.player.hasVehicle() && !mc.player.isClimbing() && itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack));
    }


    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerMove(PlayerMoveEvent e) {
        if (this.isActive()) {
            doBoost(e);
        }
    }

    private void doBoost(PlayerMoveEvent e) {
        assert mc.player != null;
        if (mc.player.getInventory().getStack(38).getItem() != Items.ELYTRA || !mc.player.isFallFlying() || mc.player.isTouchingWater() || mc.player.isInLava() || !mc.player.isFallFlying())
            return;

        if (cruiseControl.get()) {
            if (mc.options.jumpKey.isPressed()) height++;
            else if (mc.options.sneakKey.isPressed()) height--;
            if (forceHeight.get()) height = manualHeight.get();

            if (currentPlayerSpeed >= minUpSpeed.get()) mc.player.setPitch((float) MathHelper.clamp(MathHelper.wrapDegrees
                (Math.toDegrees(Math.atan2((height - mc.player.getY()) * -1.0, 10))), -50, 50));

            else mc.player.setPitch(0.25F);
        }

        if ((mc.options.jumpKey.isPressed() || !onlySpace.get() || cruiseControl.get())) {
            double[] m = forwardWithoutStrafe((factor.get() / 10f)); // TODO event is final here
            e.movement = new Vec3d(e.movement.x + m[0],e.movement.y,e.movement.z + m[1]);

        }

        double speed = Math.hypot(e.movement.x, e.movement.z);

        if (speedLimit.get() && speed > maxSpeed.get()) {
            e.movement = new Vec3d(e.movement.x * maxSpeed.get() / speed, e.movement.y, e.movement.z * maxSpeed.get() / speed);
        }

        mc.player.setVelocity(e.movement.x, e.movement.y, e.movement.z);
    }
}

/*
if (currentPlayerSpeed >= minUpSpeed.get()) mc.player.setPitch((float) MathHelper.clamp(MathHelper.wrapDegrees
                (Math.toDegrees(Math.atan2((height - mc.player.getY()) * -1.0, 10))), -50, 50));

            else mc.player.setPitch(0.25F);
 */
