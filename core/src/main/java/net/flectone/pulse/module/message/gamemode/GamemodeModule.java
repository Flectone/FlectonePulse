package net.flectone.pulse.module.message.gamemode;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class GamemodeModule extends AbstractModuleMessage<Localization.Message.Gamemode> {

    private final Message.Gamemode message;
    private final Permission.Message.Gamemode permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public GamemodeModule(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getGamemode());

        this.message = fileResolver.getMessage().getGamemode();
        this.permission = fileResolver.getPermission().getMessage().getGamemode();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            MinecraftTranslationKeys key = event.getKey();
            if (!key.startsWith("commands.gamemode.success") && key != MinecraftTranslationKeys.GAMEMODE_CHANGED) return;

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().isEmpty()) return;

            String target = event.getUserName();
            String gamemodeKey = "gameMode.survival";
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
    public void send(TranslatableMessageEvent event, String gameModeKey, String target) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUserUUID());
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        boolean isSelf = fPlayer.equals(fTarget);
        String gamemode = gameModeKey.split("\\.")[1];

        // for sender
        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (isSelf ? s.getFormatSelf() : s.getFormatOther()).replace("<gamemode>", gamemode))
                .sound(getSound())
                .sendBuilt();
    }
}
