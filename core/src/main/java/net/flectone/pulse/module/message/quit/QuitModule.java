package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.MinecraftTranslationKeys;

@Singleton
public class QuitModule extends AbstractModuleMessage<Localization.Message.Quit> {

    @Getter private final Message.Quit message;
    private final Permission.Message.Quit permission;
    private final IntegrationModule integrationModule;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public QuitModule(FileResolver fileResolver,
                      IntegrationModule integrationModule,
                      EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getQuit());

        this.message = fileResolver.getMessage().getQuit();
        this.permission = fileResolver.getPermission().getMessage().getQuit();
        this.integrationModule = integrationModule;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(QuitPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .tag(MessageTag.QUIT)
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
