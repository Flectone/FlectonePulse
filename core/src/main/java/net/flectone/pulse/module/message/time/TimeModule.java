package net.flectone.pulse.module.message.time;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.time.listener.TimePulseListener;
import net.flectone.pulse.module.message.time.model.TimeMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.Strings;

@Singleton
public class TimeModule extends AbstractModuleLocalization<Localization.Message.Time> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TimeModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(MessageType.TIME);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(TimePulseListener.class);
    }

    @Override
    public Message.Time config() {
        return fileResolver.getMessage().getTime();
    }

    @Override
    public Permission.Message.Time permission() {
        return fileResolver.getPermission().getMessage().getTime();
    }

    @Override
    public Localization.Message.Time localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getTime();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String time) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(TimeMetadata.<Localization.Message.Time>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> Strings.CS.replace(
                        translationKey == MinecraftTranslationKey.COMMANDS_TIME_QUERY ? localization.getQuery() : localization.getSet(),
                        "<time>",
                        time
                ))
                .time(time)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
