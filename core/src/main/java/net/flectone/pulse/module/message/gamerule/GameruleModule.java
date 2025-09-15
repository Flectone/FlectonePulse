package net.flectone.pulse.module.message.gamerule;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Gamerule message;
    private final Permission.Message.Gamerule permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public GameruleModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getGamerule(), MessageType.GAMERULE);

        this.message = fileResolver.getMessage().getGamerule();
        this.permission = fileResolver.getPermission().getMessage().getGamerule();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(GamerulePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Gamerule gamerule) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(GameruleMetadata.<Localization.Message.Gamerule>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_GAMERULE_QUERY ? localization.getFormatQuery() : localization.getFormatSet(),
                        new String[]{"<gamerule>", "<value>"},
                        new String[]{gamerule.name(), gamerule.value()}
                ))
                .gamerule(gamerule)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
