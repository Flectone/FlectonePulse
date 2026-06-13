package net.flectone.pulse.processing.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
public class ComponentSerializer {

    private final Gson gson;

    @NonNull
    public String toStandard(@NonNull Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    @NonNull
    public String toPlain(@NonNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @NonNull
    public String toLegacy(@NonNull Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @NonNull
    public String toJson(@NonNull Component component) {
        return gson.toJson(component);
    }

    @NonNull
    public String toJson(@NonNull PlayerPreLoginEvent playerPreLoginEventWithKickReason) {
        return gson.toJson(playerPreLoginEventWithKickReason.kickReason());
    }

    @NonNull
    public JsonElement toJsonTree(@NonNull Component component) {
        return gson.toJsonTree(component);
    }

    @NonNull
    public Component fromStandard(@NonNull String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    @NonNull
    public Component fromPlain(@NonNull String string) {
        return PlainTextComponentSerializer.plainText().deserialize(string);
    }

    @NonNull
    public Component fromLegacy(@NonNull String string) {
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    @NonNull
    public Component fromJson(String string) {
        return gson.fromJson(string, Component.class);
    }

    @NonNull
    public Component fromJsonTree(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, Component.class);
    }

}
