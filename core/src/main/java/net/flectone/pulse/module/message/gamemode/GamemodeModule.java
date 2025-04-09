package net.flectone.pulse.module.message.gamemode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.gamemode.listener.GamemodePacketListener;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;

@Singleton
public class GamemodeModule extends AbstractModuleMessage<Localization.Message.Gamemode> {

    private final Message.Gamemode message;
    private final Permission.Message.Gamemode permission;

    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public GamemodeModule(FileManager fileManager,
                          FPlayerService fPlayerService,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getGamemode());

        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getGamemode();
        permission = fileManager.getPermission().getMessage().getGamemode();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(GamemodePacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, String key, String target) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        boolean isSelf = fPlayer.equals(fTarget);

        // for sender
        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> getString(key, isSelf ? s.getSelf() : s.getOther()))
                .sound(getSound())
                .sendBuilt();

        // for receiver
        if (!isSelf) {
            builder(fTarget)
                    .destination(message.getDestination())
                    .format(s -> getString(key, s.getSelf()))
                    .sound(getSound())
                    .sendBuilt();
        }
    }

    private String getString(String key, Localization.Message.Gamemode.Type type) {
        return switch (key.split("\\.")[1]) {
            case "creative" -> type.getCreative();
            case "survival" -> type.getSurvival();
            case "adventure" -> type.getAdventure();
            case "spectator" -> type.getSpectator();
            default -> "";
        };
    }
}
