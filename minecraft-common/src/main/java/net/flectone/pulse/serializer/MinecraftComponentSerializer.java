package net.flectone.pulse.serializer;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MinecraftComponentSerializer extends ComponentSerializerImpl {

    @Inject
    public MinecraftComponentSerializer() {
        super(AdventureSerializer.serializer().gson().serializer());
    }

}
