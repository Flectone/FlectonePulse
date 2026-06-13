package net.flectone.pulse.listener.module;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.command.sprite.SpriteModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.serverlink.MinecraftServerlinkModule;
import net.flectone.pulse.module.message.tab.TabModule;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPulseModuleEnableListener implements PulseListener {

    private final MinecraftPacketProvider packetProvider;
    private final FLogger fLogger;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        ModuleSimple eventModule = event.module();
        if (eventModule instanceof BubbleModule
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)) {
            fLogger.warning("Bubble module is only supported in Minecraft versions 1.13+");
            return event.withCancelled(true);
        }

        if (eventModule instanceof MinecraftServerlinkModule
                && packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_21)) {
            fLogger.warning("Server link module is only supported in Minecraft versions 1.21+");
            return event.withCancelled(true);
        }

        if (eventModule instanceof TabModule
                && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
            fLogger.warning("Server link module is only supported in Minecraft versions 1.8.8, 1.8.9 and 1.10+");
            return event.withCancelled(true);
        }

        if (eventModule instanceof SpriteModule
                && packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_21_9)) {
            fLogger.warning("Sprite module is only supported in Minecraft versions 1.21.9+");
            return event.withCancelled(true);
        }

        return event;
    }

}
