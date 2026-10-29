package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.sponge.factory.SpongePacketEventsBuilder;
import lombok.RequiredArgsConstructor;
import org.spongepowered.plugin.PluginContainer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpongePacketEventsAdapter implements MinecraftPacketEventsAdapter {

    private final PluginContainer pluginContainer;

    @Override
    public void load() {
        // configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
        PacketEvents.setAPI(SpongePacketEventsBuilder.build(pluginContainer));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);
        PacketEvents.getAPI().load();
    }

}
