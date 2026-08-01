package net.flectone.pulse.processing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperComponentSerializer {

    private final FLogger fLogger;

    @NonNull
    public Optional<String> toPlain(@NonNull Component component) {
        try {
            return Optional.of(PlainTextComponentSerializer.plainText().serialize(component));
        } catch (Exception _) {
            fLogger.warning("Failed to plain serialize component: %s", component);

            return Optional.empty();
        }
    }

    @NonNull
    public Optional<Component> fromJson(String json) {
        try {
            return Optional.of(GsonComponentSerializer.gson().deserialize(json));
        } catch (Exception _) {
            fLogger.warning("Failed to deserialize json: %s", json);

            return Optional.empty();
        }
    }

}
