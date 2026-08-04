package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPacketEventsAdapter implements MinecraftPacketEventsAdapter {

    private final Plugin plugin;

    @Override
    public void load() {
        // configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false).debug(false);
        PacketEvents.getAPI().load();
    }

}
