package net.flectone.pulse.module.message.kill;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.kill.listener.KillPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MinecraftTranslationKeys;

import java.util.UUID;

@Singleton
public class KillModule extends AbstractModuleMessage<Localization.Message.Kill> {

    private final Message.Kill message;
    private final Permission.Message.Kill permission;

    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public KillModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getKill());

        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getKill();
        permission = fileManager.getPermission().getMessage().getKill();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(KillPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key, String value, FEntity fEntity) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FEntity fTarget = fPlayer;

        if (key == MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_SINGLE && fEntity != null) {
            fTarget = fPlayerService.getFPlayer(fEntity.getUuid());

            if (fTarget.isUnknown()) {
                fTarget = fEntity;
            }
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> key == MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_MULTIPLE
                        ? s.getMultiple().replace("<count>", value)
                        : s.getSingle()
                )
                .sound(getSound())
                .sendBuilt();
    }
}
