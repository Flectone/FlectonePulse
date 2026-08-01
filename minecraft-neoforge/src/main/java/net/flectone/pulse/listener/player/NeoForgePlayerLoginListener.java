package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePlayerLoginListener {

    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final ComponentSerializer componentSerializer;
    private final ReflectionResolver reflectionResolver;

    private MethodHandle jsonParseStringHandle;

    public void onPreLogin(RegisterConfigurationTasksEvent event) {
        if (!(event.getListener() instanceof ServerConfigurationPacketListenerImpl packetListener)) {
            return;
        }

        GameProfile profile = packetListener.getOwner();
        if (profile == null) return;

        UUID uuid = profile.id();
        String name = profile.name();

        playerPreLoginProcessor.processLogin(uuid, name, loginEvent -> {
            String json = componentSerializer.toJson(loginEvent);
            packetListener.disconnect(fromJson(json));
        });
    }

    // we are relocating Adventure and Gson, so we can't call methods directly
    // but I think this approach is better than creating a separate module in project and using it
    @SuppressWarnings("unchecked")
    private net.minecraft.network.chat.Component fromJson(String json) {
        if (jsonParseStringHandle == null) {
            jsonParseStringHandle = resolveJsonParseString();
        }

        try {
            Object jsonElement = jsonParseStringHandle.invoke(json);
            DynamicOps<Object> ops = (DynamicOps<Object>) (DynamicOps<?>) com.mojang.serialization.JsonOps.INSTANCE;

            return ComponentSerialization.CODEC.parse(ops, jsonElement).getOrThrow();
        } catch (Throwable e) {
            return net.minecraft.network.chat.Component.empty();
        }
    }

    private MethodHandle resolveJsonParseString() {
        Class<?> jsonParserClass = reflectionResolver.resolveClass("com.google.", "gson.JsonParser");
        if (jsonParserClass == null) return null;

        return reflectionResolver.unreflectMethod(jsonParserClass, "parseString", String.class);
    }

}
