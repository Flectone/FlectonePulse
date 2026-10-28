package net.flectone.pulse.serializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.kyori.adventure.text.Component;

@Singleton
public class FabricComponentSerializer extends MinecraftComponentSerializer {

    private final FabricVersionAdapter fabricVersionAdapter;

    @Inject
    public FabricComponentSerializer(FabricVersionAdapter fabricVersionAdapter) {
        this.fabricVersionAdapter = fabricVersionAdapter;
    }

    public net.minecraft.network.chat.Component toFabric(Component component) throws Throwable {
        return toFabric(toJson(component));
    }

    public net.minecraft.network.chat.Component toFabric(String json) throws Throwable {
        return fabricVersionAdapter.fromJson(json);
    }

}
