package net.anse.namethatvillage.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.anse.namethatvillage.NameThatVillage;

public record ShowVillageTitlePacket(String title) implements CustomPacketPayload {
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final Type<ShowVillageTitlePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NameThatVillage.MOD_ID, "village_title"));

    private static final StreamCodec<FriendlyByteBuf, String> STRING_CODEC =
            StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);

    public static final StreamCodec<FriendlyByteBuf, ShowVillageTitlePacket> STREAM_CODEC =
            StreamCodec.composite(
                    STRING_CODEC,
                    ShowVillageTitlePacket::title,
                    ShowVillageTitlePacket::new
            );
}
