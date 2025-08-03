package net.flectone.pulse.module.message.gamemode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.gamemode.listener.GamemodePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class GamemodeModule extends AbstractModuleLocalization<Localization.Message.Gamemode> {

    private final Message.Gamemode message;
    private final Permission.Message.Gamemode permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public GamemodeModule(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          ListenerRegistry listenerRegistry,
                          PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getGamemode());

        this.message = fileResolver.getMessage().getGamemode();
        this.permission = fileResolver.getPermission().getMessage().getGamemode();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(GamemodePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, String gamemodeKey, String target) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        boolean isSelf = fPlayer.equals(fTarget);

        String gamemode = gamemodeKey.isEmpty()
                ? platformPlayerAdapter.getGamemode(fTarget).name().toLowerCase()
                : gamemodeKey.split("\\.")[1];

        // for sender
        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (isSelf ? s.getFormatSelf() : s.getFormatOther()).replace("<gamemode>", gamemode))
                .sound(getSound())
                .sendBuilt();
    }
}
