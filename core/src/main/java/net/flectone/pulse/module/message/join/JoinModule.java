package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
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
public class JoinModule extends AbstractModuleMessage<Localization.Message.Join> {

    @Getter private final Message.Join message;
    private final Permission.Message.Join permission;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public JoinModule(FileResolver fileResolver,
                      PlatformPlayerAdapter platformPlayerAdapter,
                      IntegrationModule integrationModule,
                      EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getJoin());

        this.message = fileResolver.getMessage().getJoin();
        this.permission = fileResolver.getPermission().getMessage().getJoin();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.integrationModule =  integrationModule;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_JOIN, this::send);
        eventProcessRegistry.registerMessageHandler(event -> {
            if (event.getKey() != MinecraftTranslationKeys.MULTIPLAYER_PLAYER_JOINED) return;

            event.cancel();
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        boolean hasPlayedBefore = platformPlayerAdapter.hasPlayedBefore(fPlayer);

        builder(fPlayer)
                .tag(MessageTag.JOIN)
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
