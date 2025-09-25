package net.flectone.pulse.module.message.gamerule;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.gamerule.listener.GamerulePulseListener;
import net.flectone.pulse.module.message.gamerule.model.Gamerule;
import net.flectone.pulse.module.message.gamerule.model.GameruleMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class GameruleModule extends AbstractModuleLocalization<Localization.Message.Gamerule> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public GameruleModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(MessageType.GAMERULE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(GamerulePulseListener.class);
    }

    @Override
    public Message.Gamerule config() {
        return fileResolver.getMessage().getGamerule();
    }

    @Override
    public Permission.Message.Gamerule permission() {
        return fileResolver.getPermission().getMessage().getGamerule();
    }

    @Override
    public Localization.Message.Gamerule localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getGamerule();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Gamerule gamerule) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(GameruleMetadata.<Localization.Message.Gamerule>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_GAMERULE_QUERY ? localization.getFormatQuery() : localization.getFormatSet(),
                        new String[]{"<gamerule>", "<value>"},
                        new String[]{gamerule.name(), gamerule.value()}
                ))
                .gamerule(gamerule)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
