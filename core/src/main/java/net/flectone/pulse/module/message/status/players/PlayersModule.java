package net.flectone.pulse.module.message.status.players;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.status.players.listener.PlayersPulseListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;

import java.util.List;

@Singleton
public class PlayersModule extends AbstractModuleLocalization<Localization.Message.Status.Players> {

    private final FileResolver fileResolver;
    private final PermissionChecker permissionChecker;
    private final PlatformServerAdapter platformServerAdapter;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public PlayersModule(FileResolver fileResolver,
                         PermissionChecker permissionChecker,
                         PlatformServerAdapter platformServerAdapter,
                         ListenerRegistry listenerRegistry) {
        super(MessageType.PLAYERS);

        this.fileResolver = fileResolver;
        this.permissionChecker = permissionChecker;
        this.platformServerAdapter = platformServerAdapter;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        registerPermission(permission().getBypass());

        listenerRegistry.register(PlayersPulseListener.class);
    }

    @Override
    public Message.Status.Players config() {
        return fileResolver.getMessage().getStatus().getPlayers();
    }

    @Override
    public Permission.Message.Status.Players permission() {
        return fileResolver.getPermission().getMessage().getStatus().getPlayers();
    }

    @Override
    public Localization.Message.Status.Players localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getStatus().getPlayers();
    }

    public boolean isAllowed(FPlayer fPlayer) {
        if (!isEnable()) return true;
        if (!config().isControl()) return true;

        if (isModuleDisabledFor(fPlayer)) return true;
        if (permissionChecker.check(fPlayer, permission().getBypass())) return true;

        int online = platformServerAdapter.getOnlinePlayerCount();
        return online < config().getMax();
    }

    public List<Localization.Message.Status.Players.Sample> getSamples(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return localization(fPlayer).getSamples();
    }
}
