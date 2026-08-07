package net.flectone.pulse.processing.serializer;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ComponentSerializerImpl implements ComponentSerializer {

    private final Gson gson;

    @NonNull
    @Override
    public String toStandard(@NonNull Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    @NonNull
    @Override
    public String toPlain(@NonNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @NonNull
    @Override
    public String toLegacy(@NonNull Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @NonNull
    @Override
    public String toJson(@NonNull Component component) {
        return gson.toJson(component);
    }

    @Override
    public @NonNull Object toJsonTree(@NonNull Component component) {
        return gson.toJsonTree(component);
    }

    @NonNull
    @Override
    public String toJson(@NonNull PlayerPreLoginEvent playerPreLoginEventWithKickReason) {
        return gson.toJson(playerPreLoginEventWithKickReason.kickReason());
    }

    @NonNull
    @Override
    public Component fromStandard(@NonNull String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    @NonNull
    @Override
    public Component fromPlain(@NonNull String string) {
        return PlainTextComponentSerializer.plainText().deserialize(string);
    }

    @NonNull
    @Override
    public Component fromLegacy(@NonNull String string) {
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    @NonNull
    @Override
    public Component fromJson(String string) {
        return gson.fromJson(string, Component.class);
    }

}
