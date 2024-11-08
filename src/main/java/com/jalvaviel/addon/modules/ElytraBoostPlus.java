package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.utils.ElytraUtils;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;


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


    protected float currentPlayerSpeed;
    protected float height;
    private int counter;

    private double[] forwardWithoutStrafe(final double d) {
        assert mc.player != null;
        float f3 = mc.player.getYaw();
        final double d4 = d * Math.cos(Math.toRadians(f3 + 90.0f));
        final double d5 = d * Math.sin(Math.toRadians(f3 + 90.0f));
        return new double[]{d4, d5};
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
            if (recastCheck()) {
                counter++;
                if (counter >= recastDelay.get()) {
                    mc.player.startFallFlying();
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    counter = 0;
                }
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
