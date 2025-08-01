package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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
        super(localization -> localization.getMessage().getQuit());

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

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .tag(MessageType.QUIT)
                .destination(message.getDestination())
                .range(message.getRange())
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.QUIT))
                .filter(fReceiver -> integrationModule.isVanishedVisible(fPlayer, fReceiver))
                .format(Localization.Message.Quit::getFormat)
                .integration()
                .proxy()
                .sound(getSound())
                .sendBuilt();
    }
}
