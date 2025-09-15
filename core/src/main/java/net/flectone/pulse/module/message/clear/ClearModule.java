package net.flectone.pulse.module.message.clear;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Clear message;
    private final Permission.Message.Clear permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ClearModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getClear(), MessageType.CLEAR);

        this.message = fileResolver.getMessage().getClear();
        this.permission = fileResolver.getPermission().getMessage().getClear();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ClearPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Clear clear) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ClearMetadata.<Localization.Message.Clear>builder()
                .sender(fPlayer)
                .range(message.getRange())
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
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, clear.getTarget())})
                .build()
        );
    }

}
