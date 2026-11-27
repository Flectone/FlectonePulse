package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;
import net.kyori.adventure.text.Component;

@Singleton
public class BukkitMessageSender extends MinecraftMessageSender {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final MinecraftPacketProvider packetProvider;
    private final LazyInstance<PaperMessageSender> paperMessageSender;

    @Inject
    public BukkitMessageSender(MinecraftPacketSender packetSender,
                               MinecraftPacketProvider packetProvider,
                               IntegrationModule integrationModule,
                               FileFacade fileFacade,
                               ReflectionResolver reflectionResolver,
                               LazyInstance<PaperMessageSender> paperMessageSender,
                               FLogger fLogger) {
        super(packetSender, packetProvider, integrationModule, fLogger);

        this.fileFacade = fileFacade;
        this.reflectionResolver = reflectionResolver;
        this.packetProvider = packetProvider;
        this.paperMessageSender = paperMessageSender;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        if (fPlayer.isConsole() || silent
                || !fileFacade.config().internal().useVanillaMessageSender()
                || !reflectionResolver.isPaper()
                || !packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16_5)
                || !sendPaperMessage(fPlayer, component)) {
            super.sendMessage(fPlayer, component, silent);
        }
    }

    private boolean sendPaperMessage(FPlayer fPlayer, Component component) {
        try {
            String serialized = AdventureSerializer.serializer().gson().serialize(component);
            return paperMessageSender.get().sendMessage(fPlayer, serialized);
        } catch (Throwable _) {
            return false;
        }
    }
}
