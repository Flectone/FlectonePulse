package net.flectone.pulse.listener.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.serializer.SpongeComponentSerializer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.profile.GameProfile;

import java.lang.invoke.MethodHandle;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpongePlayerLoginListener {

    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final SpongeComponentSerializer componentSerializer;
    private final ReflectionResolver reflectionResolver;

    private MethodHandle setMessageHandle;

    @Listener(order = Order.LAST)
    public void onAuthEvent(ServerSideConnectionEvent.Auth event) {
        if (event.isCancelled()) return;

        GameProfile profile = event.profile();

        playerPreLoginProcessor.processLogin(profile.uuid(), profile.name().orElse(""), loginEvent -> {
            event.setCancelled(true);

            try {
                getSetMessageHandle().invoke(event, componentSerializer.toSponge(loginEvent.kickReason()));
            } catch (Throwable _) {
                // ignore
            }
        });
    }

    private MethodHandle getSetMessageHandle() {
        if (setMessageHandle == null) {
            setMessageHandle = reflectionResolver.unreflectMethod(MessageEvent.class, "setMessage", componentSerializer.getSpongeComponentClass());
        }

        return setMessageHandle;
    }

}
