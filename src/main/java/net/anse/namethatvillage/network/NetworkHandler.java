package net.anse.namethatvillage.network;

import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.anse.namethatvillage.network.packet.RecalcVillageStatusPacket;
import net.anse.namethatvillage.network.packet.ShowVillageTitlePacket;
import net.anse.namethatvillage.screen.custom.VillageBellMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class NetworkHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1.0.0").playToClient(
                ShowVillageTitlePacket.TYPE,
                ShowVillageTitlePacket.STREAM_CODEC,
                (packet, context) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null && mc.gui != null) {
                        mc.gui.setTitle(
                                Component.literal(packet.title())
                        );
                    }
                }
        );

        // Servidor: recalcular aldea
        event.registrar("1.0.0").playToServer(
                RecalcVillageStatusPacket.TYPE,
                RecalcVillageStatusPacket.STREAM_CODEC,
                (packet, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    if (player.containerMenu instanceof VillageBellMenu bellMenu) {
                        if (bellMenu.getBlockEntity() instanceof VillageBellBlockEntity bellBlock) {
                            bellBlock.recalcVillageStatus();
                        }
                    }
                }
        );
    }

    public static void sendTitleTo(ServerPlayer player, ShowVillageTitlePacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

}
