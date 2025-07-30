package net.flectone.pulse.module.message.seed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class SeedModule extends AbstractModuleMessage<Localization.Message.Seed> {

    private final Message.Seed message;
    private final Permission.Message.Seed permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public SeedModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getSeed());

        this.message = fileResolver.getMessage().getSeed();
        this.permission = fileResolver.getPermission().getMessage().getSeed();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            if (event.getKey() != MinecraftTranslationKey.COMMANDS_SEED_SUCCESS) return;

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().isEmpty()) return;

            Component firstArg = translatableComponent.args().get(0);
            String seed = switch (firstArg) {
                // modern format with chat.square_brackets
                case TranslatableComponent chatComponent when chatComponent.key().equals("chat.square_brackets")
                        && !chatComponent.args().isEmpty()
                        && chatComponent.args().get(0) instanceof TextComponent seedComponent -> seedComponent.content();
                // legacy format with extra
                case TextComponent textComponent when textComponent.content().equals("[")
                        && !textComponent.children().isEmpty()
                        && textComponent.children().get(0) instanceof TextComponent seedComponent -> seedComponent.content();
                // legacy format
                case TextComponent textComponent when !textComponent.content().isEmpty() -> textComponent.content();
                default -> null;
            };

            if (seed == null) return;

            event.cancel();
            send(event.getUserUUID(), seed);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String seed) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat().replace("<seed>", seed))
                .sound(getSound())
                .sendBuilt();
    }

}
