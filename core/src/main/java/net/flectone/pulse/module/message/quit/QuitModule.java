package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.util.MessageTag;

@Singleton
public class QuitModule extends AbstractModuleMessage<Localization.Message.Quit> {

    @Getter private final Message.Quit message;
    private final Permission.Message.Quit permission;

    private final ListenerRegistry listenerRegistry;

    @Inject
    public QuitModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry,
                      IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getQuit());

        this.listenerRegistry = listenerRegistry;

        message = fileResolver.getMessage().getQuit();
        permission = fileResolver.getPermission().getMessage().getQuit();

        addPredicate(integrationModule::isVanished);
    }

    @Override
    public void reload() {

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
                .format(Localization.Message.Quit::getFormat)
                .integration()
                .proxy()
                .sound(getSound())
                .sendBuilt();
    }
}
