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
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class ClearModule extends AbstractModuleLocalization<Localization.Message.Clear> {

    private final Message.Clear message;
    private final Permission.Message.Clear permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ClearModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getClear(), MessageType.CLEAR);

        this.message = fileResolver.getMessage().getClear();
        this.permission = fileResolver.getPermission().getMessage().getClear();
        this.fPlayerService = fPlayerService;
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
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, Clear clear) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayer;
        boolean isSingle = key == MinecraftTranslationKey.COMMANDS_CLEAR_SUCCESS_SINGLE
                || key == MinecraftTranslationKey.COMMANDS_CLEAR_SUCCESS;

        if (isSingle) {
            fTarget = fPlayerService.getFPlayer(clear.value());
            if (fTarget.isUnknown()) return;
        }

        sendMessage(ClearMetadata.<Localization.Message.Clear>builder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        isSingle ? s.getSingle() : s.getMultiple(),
                        new String[]{"<count>", "<number>"},
                        new String[]{clear.value(), clear.count()}
                ))
                .clear(clear)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
