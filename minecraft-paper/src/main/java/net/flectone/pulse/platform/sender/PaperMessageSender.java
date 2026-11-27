package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;

@Singleton
public class PaperMessageSender {

    private final FLogger fLogger;
    private final @Nullable MethodHandle serializerGetter;
    private final @Nullable MethodHandle serializeHandle;

    @Inject
    public PaperMessageSender(ReflectionResolver reflectionResolver, FLogger fLogger) {
        this.fLogger = fLogger;

        Class<?> paperAdventure = reflectionResolver.resolveClass("io.papermc.paper.adventure.", "PaperAdventure");
        Class<?> serializerType = reflectionResolver.resolveClass("net.kyori.adventure.text.serializer.", "ComponentSerializer");

        this.serializerGetter = paperAdventure == null || serializerType == null
                ? null
                : reflectionResolver.unreflect(lookup -> {
                    try {
                        return lookup.findStaticGetter(paperAdventure, "WRAPPER_AWARE_SERIALIZER", serializerType);
                    } catch (NoSuchFieldException _) {
                        return null;
                    }
                });
        this.serializeHandle = serializerType == null
                ? null
                : reflectionResolver.unreflectMethod(serializerType, "serialize", Component.class);
    }

    public boolean sendMessage(FPlayer fPlayer, String serialized) {
        Player player = Bukkit.getPlayer(fPlayer.uuid());
        if (player == null) return false;

        try {
            Component component = GsonComponentSerializer.gson().deserialize(serialized);
            if (!validate(component)) {
                return false;
            }

            player.sendMessage(component);
        } catch (Exception e) {
            fLogger.warning("Failed to deserialize message %s", e, serialized);
            return false;
        }

        return true;
    }

    private boolean validate(Component component) {
        if (serializerGetter == null || serializeHandle == null) {
            return true;
        }

        try {
            Object serializer = serializerGetter.invoke();
            serializeHandle.invoke(serializer, component);
            return true;
        } catch (Throwable _) {
            return false;
        }
    }
}
