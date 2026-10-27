package net.flectone.pulse.platform.adapter;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class FabricPayloadAdapter {

    private FabricPayloadAdapter() {
    }

    public static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        PayloadTypeRegistry.clientboundPlay().register(type, streamCodec);
        PayloadTypeRegistry.serverboundPlay().register(type, streamCodec);
    }

}
