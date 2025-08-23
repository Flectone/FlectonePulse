package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.metadata.EmptyMessageMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class JoinModule extends AbstractModuleLocalization<Localization.Message.Join> {

    private final Message.Join message;
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
        send(fPlayer, false);
    }

    @Async
    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer);

        builder(fPlayer)
                .tag(MessageType.JOIN)
                .destination(message.getDestination())
                .range(message.getRange())
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.JOIN))
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .format(s -> hasPlayedBefore || !message.isFirst() ? s.getFormat() : s.getFormatFirstTime())
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(hasPlayedBefore))
                .integration()
                .addMetadata(new EmptyMessageMetadata(MessageType.JOIN))
                .sound(getSound())
                .sendBuilt();
    }
}
