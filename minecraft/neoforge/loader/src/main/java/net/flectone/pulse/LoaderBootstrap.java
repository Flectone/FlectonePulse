package net.flectone.pulse;

import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface LoaderBootstrap {

    void onLoad();

    default void onEnable() {}

    default void onDisable() {}

    default void preNewPlayerPlace(Connection connection, ServerPlayer player) {}

    default void onPlayerLogin(Connection connection, ServerPlayer player) {}

    default void postRespawn(CallbackInfoReturnable<ServerPlayer> cir) {}

    default void configureSerialization(ChannelPipeline pipeline, PacketFlow flow) {}

}