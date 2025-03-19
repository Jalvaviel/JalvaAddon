package com.jalvaviel.addon.AntiKick.AntiKickData;

import com.jalvaviel.addon.AntiKick.PacketData.IPacketData;
import com.jalvaviel.addon.BiomeESP.ESPBiomeData.IBiomeData;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.jalvaviel.addon.modules.BiomeColorChanger.FALLBACK_KEYS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PacketDataSettingScreen extends WindowScreen {

    private final PacketDataSetting<?> setting;
    List<Class<? extends Packet<?>>> PACKETS = new ArrayList<>();
    private WTable table;
    private String filterText = "";

    public PacketDataSettingScreen(GuiTheme theme, PacketDataSetting<?> setting) {
        super(theme, "Configure Packets");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    public <T extends ICopyable<T> & ISerializable<T> & IChangeable & IPacketData<T>> void initTable() {
        for (Class<? extends Packet<?>> packet : PacketUtils.getC2SPackets()) {
            T packetData = (T) setting.get().get(packet);
            if (packetData != null && packetData.isChanged()) PACKETS.addFirst(packet);
            else PACKETS.add(packet);
        }

        for (Class<? extends Packet<?>> packet : PACKETS) {
            String name = PacketUtils.getName(packet);
            if (!StringUtils.containsIgnoreCase(name, filterText)) continue;

            T packetData = (T) setting.get().get(packet);
            table.add(theme.label(name)).expandCellX();
            table.add(theme.label((packetData != null && packetData.isChanged()) ? "*" : " "));

            WButton edit = table.add(theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                T data = packetData;
                if (data == null) data = (T) setting.defaultData.get().copy();
                mc.setScreen(data.createScreen(theme, packet, (PacketDataSetting<T>) setting));
            };

            WButton reset = table.add(theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                setting.get().remove(packet);
                setting.onChanged();

                if (packetData != null && packetData.isChanged()) {
                    table.clear();
                    initTable();
                }
            };

            table.row();
        }

        PACKETS.clear();
    }
}
