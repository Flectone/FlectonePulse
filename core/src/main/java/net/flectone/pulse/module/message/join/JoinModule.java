package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinPacketListener;
import net.flectone.pulse.util.MessageTag;

@Singleton
public class JoinModule extends AbstractModuleMessage<Localization.Message.Join> {

    @Getter private final Message.Join message;
    private final Permission.Message.Join permission;

    private final ListenerRegistry listenerRegistry;
    private final FPlayerManager fPlayerManager;
    private final IntegrationModule integrationModule;

    @Inject
    public JoinModule(FileManager fileManager,
                      ListenerRegistry listenerRegistry,
                      FPlayerManager fPlayerManager,
                      IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getJoin());

        this.listenerRegistry = listenerRegistry;
        this.fPlayerManager = fPlayerManager;
        this.integrationModule =  integrationModule;

        message = fileManager.getMessage().getJoin();
        permission = fileManager.getPermission().getMessage().getJoin();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(JoinPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, boolean checkVanish) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkVanish && integrationModule.isVanished(fPlayer)) return;

        boolean hasPlayedBefore = fPlayerManager.hasPlayedBefore(fPlayer);

        builder(fPlayer)
                .tag(MessageTag.JOIN)
                .destination(message.getDestination())
                .range(message.getRange())
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.JOIN))
                .format(s -> hasPlayedBefore || !message.isFirst() ? s.getFormat() : s.getFormatFirstTime())
                .proxy(byteArrayDataOutput -> byteArrayDataOutput.writeBoolean(hasPlayedBefore))
                .integration()
                .sound(getSound())
                .sendBuilt();
    }
}
