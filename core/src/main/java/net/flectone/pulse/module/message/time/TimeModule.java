package net.flectone.pulse.module.message.time;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Time message;
    private final Permission.Message.Time permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TimeModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getTime(), MessageType.TIME);

        this.message = fileResolver.getMessage().getTime();
        this.permission = fileResolver.getPermission().getMessage().getTime();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(TimePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String time) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(TimeMetadata.<Localization.Message.Time>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> Strings.CS.replace(
                        translationKey == MinecraftTranslationKey.COMMANDS_TIME_QUERY ? localization.getQuery() : localization.getSet(),
                        "<time>",
                        time
                ))
                .time(time)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
