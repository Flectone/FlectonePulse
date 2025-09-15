package net.flectone.pulse.module.message.gamemode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.gamemode.listener.GamemodePulseListener;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.flectone.pulse.module.message.gamemode.model.GamemodeMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;

@Singleton
public class GamemodeModule extends AbstractModuleLocalization<Localization.Message.Gamemode> {

    private final Message.Gamemode message;
    private final Permission.Message.Gamemode permission;
    private final ListenerRegistry listenerRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public GamemodeModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry,
                          PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getGamemode(), MessageType.GAMEMODE);

        this.message = fileResolver.getMessage().getGamemode();
        this.permission = fileResolver.getPermission().getMessage().getGamemode();
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
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Gamemode gamemode) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (!(gamemode.getTarget() instanceof FPlayer fTarget)) return;

        boolean isSelf = fPlayer.equals(fTarget);
        boolean isDefaultGamemodeCommand = translationKey == MinecraftTranslationKey.COMMANDS_DEFAULTGAMEMODE_SUCCESS;

        String gamememodeName = gamemode.getName() == null
                ? "gameMode." + platformPlayerAdapter.getGamemode(fTarget).name().toLowerCase()
                : gamemode.getName();

        sendMessage(GamemodeMetadata.<Localization.Message.Gamemode>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> Strings.CS.replace(isDefaultGamemodeCommand ? localization.getSetDefault() : isSelf ? localization.getSelf() : localization.getOther(),
                        "<gamemode>",
                        gamememodeName
                ))
                .gamemode(gamemode)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, fTarget)})
                .build()
        );
    }
}
