package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitPulseListener;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class QuitModule extends AbstractModuleLocalization<Localization.Message.Quit> {

    private final FileResolver fileResolver;
    private final IntegrationModule integrationModule;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public QuitModule(FileResolver fileResolver,
                      IntegrationModule integrationModule,
                      ListenerRegistry listenerRegistry) {
        super(MessageType.QUIT);

        this.fileResolver = fileResolver;
        this.integrationModule = integrationModule;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(QuitPulseListener.class);
    }

    @Override
    public Message.Quit config() {
        return fileResolver.getMessage().getQuit();
    }

    @Override
    public Permission.Message.Quit permission() {
        return fileResolver.getPermission().getMessage().getQuit();
    }

    @Override
    public Localization.Message.Quit localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getQuit();
    }

    @Async
    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(QuitMetadata.<Localization.Message.Quit>builder()
                .sender(fPlayer)
                .format(Localization.Message.Quit::getFormat)
                .ignoreVanish(ignoreVanish)
                .destination(config().getDestination())
                .range(config().getRange())
                .sound(getModuleSound())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .integration()
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(ignoreVanish))
                .build()
        );
    }
}
