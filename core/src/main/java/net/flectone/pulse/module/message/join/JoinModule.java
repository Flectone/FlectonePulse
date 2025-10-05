package net.flectone.pulse.module.message.join;

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
import net.flectone.pulse.module.message.join.listener.JoinPulseListener;
import net.flectone.pulse.module.message.join.model.JoinMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class JoinModule extends AbstractModuleLocalization<Localization.Message.Join> {

    private final FileResolver fileResolver;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public JoinModule(FileResolver fileResolver,
                      PlatformPlayerAdapter platformPlayerAdapter,
                      IntegrationModule integrationModule,
                      ListenerRegistry listenerRegistry) {
        super(MessageType.JOIN);

        this.fileResolver = fileResolver;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.integrationModule =  integrationModule;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(JoinPulseListener.class);
    }

    @Override
    public Message.Join config() {
        return fileResolver.getMessage().getJoin();
    }

    @Override
    public Permission.Message.Join permission() {
        return fileResolver.getPermission().getMessage().getJoin();
    }

    @Override
    public Localization.Message.Join localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getJoin();
    }

    @Async(delay = 5)
    public void sendLater(FPlayer fPlayer) {
        send(fPlayer, false);
    }

    @Async
    public void send(FPlayer fPlayer, boolean ignoreVanish) {
        if (isModuleDisabledFor(fPlayer)) return;

        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer);

        sendMessage(JoinMetadata.<Localization.Message.Join>builder()
                .sender(fPlayer)
                .format(s -> hasPlayedBefore || !config().isFirst() ? s.getFormat() : s.getFormatFirstTime())
                .ignoreVanish(ignoreVanish)
                .playedBefore(hasPlayedBefore)
                .destination(config().getDestination())
                .range(config().getRange())
                .sound(getModuleSound())
                .filter(fReceiver -> ignoreVanish || integrationModule.canSeeVanished(fPlayer, fReceiver))
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeBoolean(hasPlayedBefore);
                    dataOutputStream.writeBoolean(ignoreVanish);
                })
                .integration()
                .build()
        );
    }
}
