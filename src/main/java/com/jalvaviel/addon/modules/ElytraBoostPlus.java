package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.math.Vec3d;


public class ElytraBoostPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public ElytraBoostPlus() {
        super(Addon.CATEGORY, "elytra-boost-+", "Borrowed from ThunderClient");
    }

    /*
    public final Setting<Boolean> twoBee = sgGeneral.add(new BoolSetting.Builder()
        .name("2b2t")
        .defaultValue(true)
        .build()
    );
    */

    public final Setting<Boolean> onlySpace = sgGeneral.add(new BoolSetting.Builder()
        .name("only-space")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> cruiseControl = sgGeneral.add(new BoolSetting.Builder()
        .name("cruise-control")
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> factor = sgGeneral.add(new DoubleSetting.Builder()
        .name("factor")
        .min(0.1f)
        .max(50.0f)
        .defaultValue(1.5f)
        .build()
    );

    public final Setting<Double> minUpSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-up-speed")
        .min(0.1f)
        .max(5.0f)
        .defaultValue(0.5f)
        .visible(cruiseControl::get)
        .build()
    );

    public final Setting<Boolean> forceHeight = sgGeneral.add(new BoolSetting.Builder()
        .name("force-height")
        .defaultValue(false)
        .visible(cruiseControl::get)
        .build()
    );

    public final Setting<Integer> manualHeight = sgGeneral.add(new IntSetting.Builder()
        .name("manual-height")
        .defaultValue(121)
        .sliderRange(1,256)
        .visible(forceHeight::get)
        .build()
    );

    public final Setting<Boolean> speedLimit = sgGeneral.add(new BoolSetting.Builder()
        .name("speed-limit")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> maxSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-speed")
        .min(0.1f)
        .max(510.0f)
        .defaultValue(2.5f)
        .build()
    );


    public float currentPlayerSpeed;
    public float height;
    public double[] movement;

    public double[] forwardWithoutStrafe(final double d) {
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
    public void onTick(TickEvent.Post event){
        assert mc.player != null;
        currentPlayerSpeed = (float) Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent e) {
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

            //if(twoBee.get()) {
            if (currentPlayerSpeed >= minUpSpeed.get())
                mc.player.setPitch((float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2((height - mc.player.getY()) * -1.0, 10))), -50, 50));
            else
                mc.player.setPitch(0.25F);
            //}
        }

        //if(twoBee.get()) {
        if ((mc.options.jumpKey.isPressed() || !onlySpace.get() || cruiseControl.get())) {
            double[] m = forwardWithoutStrafe((factor.get() / 10f)); // TODO event is final here
            e.movement = new Vec3d(e.movement.x + m[0],e.movement.y,e.movement.z + m[1]);
            // movement[0] = (movement[0] + m[0]);
            // movement[2] = (movement[2] + m[1]);
        }
        //}

        double speed = Math.hypot(e.movement.x, e.movement.z);

        if (speedLimit.get() && speed > maxSpeed.get()) {
            e.movement = new Vec3d(e.movement.x * maxSpeed.get() / speed, e.movement.y, e.movement.z * maxSpeed.get() / speed);
            //movement[0] = (movement[0] * maxSpeed.get() / speed);
            //movement[2] = (modoBovement[2] * maxSpeed.get() / speed);
        }

        mc.player.setVelocity(e.movement.x, e.movement.y, e.movement.z);
        //mc.player.setVelocity(movement[0], movement[1], movement[2]);
    }
}
