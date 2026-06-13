package net.flectone.pulse.processing.serializer;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.Message;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

@Singleton
public class HytaleComponentSerializer extends ComponentSerializer {

    @Inject
    public HytaleComponentSerializer(Gson gson) {
        super(gson);
    }

    @NonNull
    public Message toHytale(@NonNull Component component) {
        return eu.mikart.adventure.platform.hytale.HytaleComponentSerializer.get().serialize(component);
    }

    @NonNull
    public Component fromHytale(@NonNull Message message) {
        return eu.mikart.adventure.platform.hytale.HytaleComponentSerializer.get().deserialize(message);
    }

}
