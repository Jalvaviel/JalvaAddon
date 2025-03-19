package com.jalvaviel.addon.AntiKick.PacketData;

import com.jalvaviel.addon.AntiKick.AntiKickData.PacketDataSetting;
import com.jalvaviel.addon.BiomeESP.BiomeData.BiomeDataSetting;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.ESPBiomeData;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public class PacketDataScreen extends WindowScreen {
    private final PacketData packetData;
    private final Class<? extends Packet<?>> packet;
    private final PacketDataSetting<PacketData> setting;

    public PacketDataScreen(GuiTheme theme, PacketData packetData, Class<? extends Packet<?>> packet, PacketDataSetting<PacketData> setting) {
        super(theme, "Configure Packet");

        this.packetData = packetData;
        this.packet = packet;
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        Settings settings = new Settings();
        SettingGroup sgGeneral = settings.getDefaultGroup();

        sgGeneral.add(new IntSetting.Builder()
            .name("max-packets")
            .description("Max packets to be sent.")
            .defaultValue(300)
            .sliderRange(0,1000)
            .onModuleActivated(settingInteger -> settingInteger.set(packetData.maxPackets))
            .onChanged(integer -> {
                packetData.maxPackets = integer;
                changed(packetData, packet, setting);
            })
            .build()
        );

        sgGeneral.add(new IntSetting.Builder()
            .name("packet-interval")
            .description("The packet interval in seconds.")
            .defaultValue(7)
            .sliderRange(0,60)
            .onModuleActivated(settingInteger -> settingInteger.set(packetData.packetInterval))
            .onChanged(integer -> {
                packetData.packetInterval = integer;
                changed(packetData, packet, setting);
            })
            .build()
        );

        settings.onActivated();
        add(theme.settings(settings)).expandX();
    }

    private void changed(PacketData packetData, Class<? extends Packet<?>> packet, PacketDataSetting<PacketData> setting) {
        if (!packetData.isChanged() && packet != null && setting != null) {
            setting.get().put(packet, packetData);
            setting.onChanged();
        }

        packetData.changed();
    }
}
