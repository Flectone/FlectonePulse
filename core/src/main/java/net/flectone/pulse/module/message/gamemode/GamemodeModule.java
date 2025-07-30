package net.flectone.pulse.module.message.gamemode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class GamemodeModule extends AbstractModuleMessage<Localization.Message.Gamemode> {

    private final Message.Gamemode message;
    private final Permission.Message.Gamemode permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public GamemodeModule(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          EventProcessRegistry eventProcessRegistry,
                          PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getGamemode());

        this.message = fileResolver.getMessage().getGamemode();
        this.permission = fileResolver.getPermission().getMessage().getGamemode();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            MinecraftTranslationKey key = event.getKey();
            if (!key.startsWith("commands.gamemode.success") && key != MinecraftTranslationKey.GAMEMODE_CHANGED) return;

            String target = event.getUserName();
            String gamemodeKey = "";

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().isEmpty()) {
                event.cancel();
                send(event, gamemodeKey, target);
                return;
            }

            if (translatableComponent.args().get(0) instanceof TranslatableComponent gamemodeComponent) {
                gamemodeKey = gamemodeComponent.key();
            } else if (translatableComponent.args().size() > 1
                    && translatableComponent.args().get(0) instanceof TextComponent playerComponent
                    && translatableComponent.args().get(1) instanceof TranslatableComponent gamemodeComponent) {
                target = playerComponent.content();
                gamemodeKey = gamemodeComponent.key();
            }

            event.cancel();
            send(event, gamemodeKey, target);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(TranslatableMessageEvent event, String gamemodeKey, String target) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUserUUID());
        if (checkModulePredicates(fPlayer)) return;

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
