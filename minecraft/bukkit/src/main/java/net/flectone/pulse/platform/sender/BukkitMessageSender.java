package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class BukkitMessageSender extends MinecraftMessageSender {

    private final FileFacade fileFacade;
    private final PaperMessageSender paperMessageSender;
    private final ReflectionResolver reflectionResolver;

    @Inject
    public BukkitMessageSender(MinecraftPacketSender packetSender,
                               MinecraftPacketProvider packetProvider,
                               IntegrationModule integrationModule,
                               FileFacade fileFacade,
                               PaperMessageSender paperMessageSender,
                               ReflectionResolver reflectionResolver,
                               FLogger fLogger) {
        super(packetSender, packetProvider, integrationModule, fLogger);

        this.fileFacade = fileFacade;
        this.paperMessageSender = paperMessageSender;
        this.reflectionResolver = reflectionResolver;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        if (fPlayer.isConsole() || silent
                || !fileFacade.config().internal().usePaperMessageSender()
                || !reflectionResolver.isPaper()
                || !paperMessageSender.sendMessage(fPlayer, AdventureSerializer.serializer().gson().serialize(component))) {
            super.sendMessage(fPlayer, component, silent);
        }
    }
}
