package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.NeoForgePlayerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.processing.serializer.NeoForgeComponentSerializer;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class NeoForgeMessageSender extends MinecraftMessageSender {

    private final NeoForgePlayerAdapter neoForgePlayerAdapter;
    private final NeoForgeComponentSerializer componentSerializer;
    private final FileFacade fileFacade;
    private final FLogger fLogger;

    @Inject
    public NeoForgeMessageSender(MinecraftPacketSender packetSender,
                                 MinecraftPacketProvider packetProvider,
                                 IntegrationModule integrationModule,
                                 FLogger fLogger,
                                 NeoForgePlayerAdapter neoForgePlayerAdapter,
                                 NeoForgeComponentSerializer componentSerializer,
                                 FileFacade fileFacade) {
        super(packetSender, packetProvider, integrationModule, fLogger);

        this.neoForgePlayerAdapter = neoForgePlayerAdapter;
        this.componentSerializer = componentSerializer;
        this.fileFacade = fileFacade;
        this.fLogger = fLogger;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        if (fPlayer.isConsole() || silent
                || !fileFacade.config().internal().useVanillaMessageSender()
                || !sendMessage(fPlayer, component)) {
            super.sendMessage(fPlayer, component, silent);
        }
    }

    public boolean sendMessage(FPlayer fPlayer, Component component) {
        ServerPlayer serverPlayer = neoForgePlayerAdapter.getPlayer(fPlayer.uuid());
        if (serverPlayer == null) return false;

        try {
            serverPlayer.sendSystemMessage(componentSerializer.toNeoForge(component));
        } catch (Throwable e) {
            fLogger.warning(e, "Failed to deserialize message %s", component);
            return false;
        }

        return true;
    }

}
