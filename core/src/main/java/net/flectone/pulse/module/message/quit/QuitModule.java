package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.message.quit.model.QuitMetadata;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class QuitModule extends AbstractModuleLocalization<Localization.Message.Quit> {

    private final Message.Quit message;
    private final Permission.Message.Quit permission;
    private final IntegrationModule integrationModule;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public QuitModule(FileResolver fileResolver,
                      IntegrationModule integrationModule,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getQuit(), MessageType.QUIT);

        this.message = fileResolver.getMessage().getQuit();
        this.permission = fileResolver.getPermission().getMessage().getQuit();
        this.integrationModule = integrationModule;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(QuitPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(QuitMetadata.<Localization.Message.Quit>builder()
                .sender(fPlayer)
                .format(Localization.Message.Quit::getFormat)
                .ignoreVanish(ignoreVanish)
                .destination(message.getDestination())
                .range(message.getRange())
                .sound(getModuleSound())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .integration()
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(ignoreVanish))
                .build()
        );
    }
}
