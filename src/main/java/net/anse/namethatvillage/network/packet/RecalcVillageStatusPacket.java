package net.anse.namethatvillage.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.anse.namethatvillage.NameThatVillage;

public record RecalcVillageStatusPacket() implements CustomPacketPayload {
    @Override
    public Type<RecalcVillageStatusPacket> type() {
        return TYPE;
    }

    public static final Type<RecalcVillageStatusPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(NameThatVillage.MOD_ID, "recalc_village_status"));

    public static final StreamCodec<FriendlyByteBuf, RecalcVillageStatusPacket> STREAM_CODEC =
            StreamCodec.unit(new RecalcVillageStatusPacket());
}
