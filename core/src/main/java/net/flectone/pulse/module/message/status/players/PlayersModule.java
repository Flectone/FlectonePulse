package net.flectone.pulse.module.message.status.players;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.List;

@Singleton
public class PlayersModule extends AbstractModuleMessage<Localization.Message.Status.Players> {

    @Getter private final Message.Status.Players message;
    private final Permission.Message.Status.Players permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public PlayersModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         PermissionChecker permissionChecker,
                         PlatformServerAdapter platformServerAdapter,
                         MessagePipeline messagePipeline,
                         EventProcessRegistry eventProcessRegistry) {
        super(module -> module.getMessage().getStatus().getPlayers());

        this.message = fileResolver.getMessage().getStatus().getPlayers();
        this.permission = fileResolver.getPermission().getMessage().getStatus().getPlayers();
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.platformServerAdapter = platformServerAdapter;
        this.messagePipeline = messagePipeline;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getBypass());

        eventProcessRegistry.registerHandler(Event.Type.PLAYER_PRE_LOGIN, event -> {
            PlayerPreLoginEvent playerPreLoginEvent = (PlayerPreLoginEvent) event;
            FPlayer fPlayer = playerPreLoginEvent.getPlayer();

            if (isAllowed(fPlayer)) return;

            playerPreLoginEvent.setAllowed(false);

            fPlayerService.loadSettings(fPlayer);
            fPlayerService.loadColors(fPlayer);

            String reasonMessage = resolveLocalization(fPlayer).getFull();
            Component reason = messagePipeline.builder(fPlayer, reasonMessage).build();

            playerPreLoginEvent.setKickReason(reason);
        });
    }

    public boolean isAllowed(FPlayer fPlayer) {
        if (!isEnable()) return true;
        if (!message.isControl()) return true;

        if (checkModulePredicates(fPlayer)) return true;
        if (permissionChecker.check(fPlayer, permission.getBypass())) return true;

        int online = platformServerAdapter.getOnlinePlayerCount();
        return online < message.getMax();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return resolveLocalization(fPlayer).getSamples();
    }
}
