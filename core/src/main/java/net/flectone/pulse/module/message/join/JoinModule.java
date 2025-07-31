package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class JoinModule extends AbstractModuleMessage<Localization.Message.Join> {

    @Getter private final Message.Join message;
    private final Permission.Message.Join permission;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public JoinModule(FileResolver fileResolver,
                      PlatformPlayerAdapter platformPlayerAdapter,
                      IntegrationModule integrationModule,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getJoin());

        this.message = fileResolver.getMessage().getJoin();
        this.permission = fileResolver.getPermission().getMessage().getJoin();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.integrationModule =  integrationModule;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        listenerRegistry.register(JoinPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async(delay = 5)
    public void sendLater(FPlayer fPlayer) {
        send(fPlayer);
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer);

        builder(fPlayer)
                .tag(MessageType.JOIN)
                .destination(message.getDestination())
                .range(message.getRange())
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.JOIN))
                .filter(fReceiver -> integrationModule.isVanishedVisible(fPlayer, fReceiver))
                .format(s -> hasPlayedBefore || !message.isFirst() ? s.getFormat() : s.getFormatFirstTime())
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(hasPlayedBefore))
                .integration()
                .sound(getSound())
                .sendBuilt();
    }
}
