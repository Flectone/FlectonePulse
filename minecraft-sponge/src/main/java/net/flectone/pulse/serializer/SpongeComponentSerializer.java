package net.flectone.pulse.serializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.serialization.DynamicOps;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;

@Singleton
public class SpongeComponentSerializer extends MinecraftComponentSerializer {

    private final ReflectionResolver reflectionResolver;
    private final MethodHandle jsonParseStringHandle;
    private final MethodHandle spongeDeserializeHandle;

    @Inject
    public SpongeComponentSerializer(ReflectionResolver reflectionResolver) {
        this.reflectionResolver = reflectionResolver;
        this.jsonParseStringHandle = resolveJsonParseString();
        this.spongeDeserializeHandle = resolveSpongeDeserialize();
    }

    public net.minecraft.network.chat.Component toMinecraft(Component component) throws Throwable {
        return toMinecraft(toJson(component));
    }

    // we are relocating Adventure and Gson, so we can't call methods directly
    // but I think this approach is better than creating a separate module in project and using it
    @SuppressWarnings("unchecked")
    public net.minecraft.network.chat.Component toMinecraft(String json) throws Throwable {
        Object jsonElement = jsonParseStringHandle.invoke(json);
        DynamicOps<Object> ops = (DynamicOps<Object>) (DynamicOps<?>) com.mojang.serialization.JsonOps.INSTANCE;

        return ComponentSerialization.CODEC.parse(ops, jsonElement).getOrThrow();
    }

    public Object toSponge(Component component) throws Throwable {
        return spongeDeserializeHandle.invoke(toJson(component));
    }

    public @Nullable Class<?> getSpongeComponentClass() {
        return reflectionResolver.resolveClass("net.", "kyori.adventure.text.Component");
    }

    private MethodHandle resolveJsonParseString() {
        Class<?> jsonParserClass = reflectionResolver.resolveClass("com.google.", "gson.JsonParser");
        if (jsonParserClass == null) return null;

        return reflectionResolver.unreflectMethod(jsonParserClass, "parseString", String.class);
    }

    private MethodHandle resolveSpongeDeserialize() {
        Class<?> gsonSerializerClass = reflectionResolver.resolveClass("net.", "kyori.adventure.text.serializer.gson.GsonComponentSerializer");
        if (gsonSerializerClass == null) return null;

        MethodHandle gsonHandle = reflectionResolver.unreflectMethod(gsonSerializerClass, "gson");
        MethodHandle deserializeHandle = reflectionResolver.unreflectMethod(gsonSerializerClass, "deserialize", Object.class);
        if (gsonHandle == null || deserializeHandle == null) return null;

        try {
            return deserializeHandle.bindTo(gsonHandle.invoke());
        } catch (Throwable _) {
            return null;
        }
    }

}
