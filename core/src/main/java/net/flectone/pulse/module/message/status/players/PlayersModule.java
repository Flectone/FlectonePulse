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
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.status.players.listener.PlayersPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

import java.util.List;

@Singleton
public class PlayersModule extends AbstractModuleMessage<Localization.Message.Status.Players> {

    @Getter private final Message.Status.Players message;
    private final Permission.Message.Status.Players permission;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public PlayersModule(FileResolver fileResolver,
                         PermissionChecker permissionChecker,
                         PlatformServerAdapter platformServerAdapter,
                         ListenerRegistry listenerRegistry) {
        super(module -> module.getMessage().getStatus().getPlayers());

        this.message = fileResolver.getMessage().getStatus().getPlayers();
        this.permission = fileResolver.getPermission().getMessage().getStatus().getPlayers();
        this.permissionChecker = permissionChecker;
        this.platformServerAdapter = platformServerAdapter;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getBypass());

        listenerRegistry.register(PlayersPulseListener.class);
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
