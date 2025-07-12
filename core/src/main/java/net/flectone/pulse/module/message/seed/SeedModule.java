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
import net.flectone.pulse.util.MinecraftTranslationKeys;
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
            if (event.getKey() != MinecraftTranslationKeys.COMMANDS_SEED_SUCCESS) return;

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().isEmpty()) return;
            if (!(translatableComponent.args().get(0) instanceof TranslatableComponent chatComponent)) return;
            if (chatComponent.args().isEmpty()) return;
            if (!(chatComponent.args().get(0) instanceof TextComponent seedComponent)) return;

            event.cancel();

            send(event.getUserUUID(), seedComponent.content());
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
