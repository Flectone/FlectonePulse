package net.flectone.pulse.serializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.serialization.DynamicOps;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.lang.invoke.MethodHandle;

@Singleton
public class NeoForgeComponentSerializer extends MinecraftComponentSerializer {

    private final ReflectionResolver reflectionResolver;
    private final MethodHandle jsonParseStringHandle;

    @Inject
    public NeoForgeComponentSerializer(ReflectionResolver reflectionResolver) {
        this.reflectionResolver = reflectionResolver;
        this.jsonParseStringHandle = resolveJsonParseString();
    }

    public net.minecraft.network.chat.Component toNeoForge(Component component) throws Throwable {
        return toNeoForge(toJson(component));
    }

    // we are relocating Adventure and Gson, so we can't call methods directly
    // but I think this approach is better than creating a separate module in project and using it
    @SuppressWarnings("unchecked")
    public net.minecraft.network.chat.Component toNeoForge(String json) throws Throwable {
        Object jsonElement = jsonParseStringHandle.invoke(json);
        DynamicOps<Object> ops = (DynamicOps<Object>) (DynamicOps<?>) com.mojang.serialization.JsonOps.INSTANCE;

        return ComponentSerialization.CODEC.parse(ops, jsonElement).getOrThrow();
    }

    private MethodHandle resolveJsonParseString() {
        Class<?> jsonParserClass = reflectionResolver.resolveClass("com.google.", "gson.JsonParser");
        if (jsonParserClass == null) return null;

        return reflectionResolver.unreflectMethod(jsonParserClass, "parseString", String.class);
    }

}
