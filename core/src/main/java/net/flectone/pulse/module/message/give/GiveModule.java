package net.flectone.pulse.module.message.give;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.give.listener.GivePulseListener;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.module.message.give.model.GiveMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class GiveModule extends AbstractModuleLocalization<Localization.Message.Give> {

    private final Message.Give message;
    private final Permission.Message.Give permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public GiveModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getGive(), MessageType.GIVE);

        this.message = fileResolver.getMessage().getGive();
        this.permission = fileResolver.getPermission().getMessage().getGive();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(GivePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Give give) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (give.isIncorrect()) return;

        sendMessage(GiveMetadata.<Localization.Message.Give>builder()
                .sender(give.target() == null ? fPlayer : give.target())
                .filterPlayer(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        translationKey == MinecraftTranslationKey.COMMANDS_GIVE_SUCCESS_MULTIPLE ? s.getMultiple() : s.getSingle(),
                        new String[]{"<amount>", "<item>", "<count>"},
                        new String[]{give.amount(), give.item(), StringUtils.defaultString(give.count())}
                ))
                .give(give)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}