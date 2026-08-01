package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.flectone.pulse.mixin.ServerLoginPacketListenerImplAccessor;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.util.file.FileFacade;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlayerLoginListener {

    private final FileFacade fileFacade;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final ComponentSerializer componentSerializer;
    private final ReflectionResolver reflectionResolver;

    private MethodHandle jsonParseStringHandle;

    public void onPreLogin(ServerLoginPacketListenerImpl netHandler, MinecraftServer server, PacketSender packetSender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
        if (fileFacade.config().internal().usePacketLoginListener()) return;
        if (!netHandler.isAcceptingMessages()) return;

        GameProfile profile = ((ServerLoginPacketListenerImplAccessor) netHandler).getAuthenticatedProfile();
        UUID uuid = profile.id();
        String name = profile.name();

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            String json = componentSerializer.toJson(loginEvent);
            netHandler.disconnect(fromJson(json));
        });
    }

    // we are relocating Adventure and Gson, so we can't call methods directly
    // but I think this approach is better than creating a separate module in project and using it
    @SuppressWarnings("unchecked")
    private Component fromJson(String json) {
        if (jsonParseStringHandle == null) {
            jsonParseStringHandle = resolveJsonParseString();
        }

        try {
            Object jsonElement = jsonParseStringHandle.invoke(json);
            DynamicOps<Object> ops = (DynamicOps<Object>) (DynamicOps<?>) com.mojang.serialization.JsonOps.INSTANCE;

            return ComponentSerialization.CODEC.parse(ops, jsonElement).getOrThrow();
        } catch (Throwable e) {
            return Component.empty();
        }
    }

    private MethodHandle resolveJsonParseString() {
        Class<?> jsonParserClass = reflectionResolver.resolveClass("com.google.", "gson.JsonParser");
        if (jsonParserClass == null) return null;

        return reflectionResolver.unreflectMethod(jsonParserClass, "parseString", String.class);
    }
}