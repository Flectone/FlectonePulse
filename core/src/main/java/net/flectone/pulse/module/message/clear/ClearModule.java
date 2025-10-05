package net.flectone.pulse.module.message.clear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.clear.listener.ClearPulseListener;
import net.flectone.pulse.module.message.clear.model.Clear;
import net.flectone.pulse.module.message.clear.model.ClearMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class ClearModule extends AbstractModuleLocalization<Localization.Message.Clear> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ClearModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        super(MessageType.CLEAR);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(ClearPulseListener.class);
    }

    @Override
    public Message.Clear config() {
        return fileResolver.getMessage().getClear();
    }

    @Override
    public Permission.Message.Clear permission() {
        return fileResolver.getPermission().getMessage().getClear();
    }

    @Override
    public Localization.Message.Clear localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getClear();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Clear clear) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ClearMetadata.<Localization.Message.Clear>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_CLEAR_SUCCESS_MULTIPLE -> localization.getMultiple();
                            case COMMANDS_CLEAR_SUCCESS_SINGLE, COMMANDS_CLEAR_SUCCESS -> localization.getSingle();
                            default -> "";
                        },
                        new String[]{"<items>", "<players>"},
                        new String[]{clear.getItems(), StringUtils.defaultString(clear.getPlayers())}
                ))
                .clear(clear)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, clear.getTarget())})
                .build()
        );
    }
}
