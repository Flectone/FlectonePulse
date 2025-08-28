package net.flectone.pulse.module.message.gamemode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.flectone.pulse.module.message.gamemode.model.GamemodeMetadata;
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
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;

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
        super(localization -> localization.getMessage().getGamemode(), MessageType.GAMEMODE);

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
    public void send(FPlayer fPlayer, Gamemode gamemode) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(gamemode.target());
        if (fTarget.isUnknown()) return;

        boolean isSelf = fPlayer.equals(fTarget);

        String gamemodeType = gamemode.type().isEmpty()
                ? platformPlayerAdapter.getGamemode(fTarget).name().toLowerCase()
                : gamemode.type().split("\\.")[1];

        // for sender
        sendMessage(GamemodeMetadata.<Localization.Message.Gamemode>builder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(s -> Strings.CS.replace(
                        isSelf ? s.getFormatSelf() : s.getFormatOther(),
                        "<gamemode>",
                        gamemodeType
                ))
                .gamemode(gamemode)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
