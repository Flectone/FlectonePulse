package net.flectone.pulse.module.message.kill;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.event.metadata.KillMessageMetadata;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.kill.listener.KillPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.apache.commons.lang3.Strings;

@Singleton
public class KillModule extends AbstractModuleLocalization<Localization.Message.Kill> {

    private final Message.Kill message;
    private final Permission.Message.Kill permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public KillModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getKill());

        this.message = fileResolver.getMessage().getKill();
        this.permission = fileResolver.getPermission().getMessage().getKill();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(KillPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey key, Kill kill) {
        if (isModuleDisabledFor(fPlayer)) return;

        FEntity fTarget = fPlayer;

        boolean isSingle = key == MinecraftTranslationKey.COMMANDS_KILL_SUCCESS_SINGLE
                || key == MinecraftTranslationKey.COMMANDS_KILL_SUCCESS;

        if (isSingle && kill.getEntityUUID() != null) {
            fTarget = fPlayerService.getFPlayer(kill.getEntityUUID());

            if (fTarget.isUnknown()) {
                fTarget = kill.fEntity();
            }
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> key == MinecraftTranslationKey.COMMANDS_KILL_SUCCESS_MULTIPLE
                        ? Strings.CS.replace(s.getMultiple(), "<count>", kill.value())
                        : s.getSingle()
                )
                .sound(getSound())
                .addMetadata(new KillMessageMetadata(kill))
                .sendBuilt();
    }
}
