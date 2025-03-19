package com.jalvaviel.addon.modules;

import com.jalvaviel.addon.Addon;
import com.jalvaviel.addon.AntiKick.AntiKickData.PacketDataSetting;
import com.jalvaviel.addon.AntiKick.PacketData.PacketData;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AntiKick extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Set<Class<? extends Packet<?>>>> packets = sgGeneral.add(new PacketListSetting.Builder()
        .name("packets")
        .description("Packets to limit.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    public final Setting<PacketData> defaultPacketConfig = sgGeneral.add(new GenericSetting.Builder<PacketData>()
        .name("default-packet-config")
        .description("Default packet config.")
        .defaultValue(new PacketData(300, 7))
        .build()
    );

    public final Setting<Map<Class<? extends Packet<?>>, PacketData>> packetConfigs = sgGeneral.add(new PacketDataSetting.Builder<PacketData>()
        .name("packet-configs")
        .description("Config for each packet.")
        .defaultData(defaultPacketConfig)
        .build()
    );

    public final Setting<Boolean> notifications = sgGeneral.add(new BoolSetting.Builder()
        .name("notifications")
        .description("Enable packet notifications when cancelled.")
        .defaultValue(false)
        .build()
    );

    private int tickCounter;
    private int secondCounter;
    private final Map<Class<? extends Packet<?>>, Map<Integer, Integer>> packetCounts = new HashMap<>();

    public AntiKick() {
        super(Addon.CATEGORY, "anti-kick", "Prevents packet-based kicks by limiting packet rates.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        secondCounter = 0;
        packetCounts.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (tickCounter % 20 == 0) {
            secondCounter++;
            tickCounter = 0;

            for (Map<Integer, Integer> counts : packetCounts.values()) {
                counts.entrySet().removeIf(entry -> entry.getKey() < secondCounter - entry.getValue());
            }
        }
        tickCounter++;
    }

    @EventHandler(priority = EventPriority.HIGHEST + 2)
    private void onSendPacket(PacketEvent.Send event) {
        Class<? extends Packet<?>> packet = (Class<? extends Packet<?>>) event.packet.getClass();
        if (packets.get().contains(packet)) {
            PacketData data = packetConfigs.get().getOrDefault(packet, defaultPacketConfig.get());

            Map<Integer, Integer> counts = packetCounts.computeIfAbsent(packet, k -> new HashMap<>());

            counts.put(secondCounter, counts.getOrDefault(secondCounter, 0) + 1);

            int totalPackets = counts.entrySet().stream()
                .filter(entry -> entry.getKey() >= secondCounter - data.packetInterval)
                .mapToInt(Map.Entry::getValue)
                .sum();

            if (totalPackets > data.maxPackets) {
                if (notifications.get()) info("A packet has been cancelled: "+PacketUtils.getName(packet));
                event.cancel();
            }
        }
    }
}
