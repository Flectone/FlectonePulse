package net.flectone.pulse.module.message.setblock;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Singleton
public class SetblockModule extends AbstractModuleMessage<Localization.Message.Setblock> {

    private final Message.Setblock message;
    private final Permission.Message.Setblock permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public SetblockModule(FileResolver fileResolver,
                          FPlayerService fPlayerService,
                          EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getSetblock());

        this.message = fileResolver.getMessage().getSetblock();
        this.permission = fileResolver.getPermission().getMessage().getSetblock();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SetblockPacketListener.class);
        eventProcessRegistry.registerMessageHandler(event -> {

            TranslatableComponent translatableComponent = event.getComponent();
            List<Component> translationArguments = translatableComponent.args();
            if (translationArguments.size() < 3) return;
            if (!(translationArguments.get(0) instanceof TextComponent xComponent)) return;
            if (!(translationArguments.get(1) instanceof TextComponent yComponent)) return;
            if (!(translationArguments.get(2) instanceof TextComponent zComponent)) return;

            String y = yComponent.content();
            String x = xComponent.content();
            String z = zComponent.content();

            event.cancel();

            send(event.getUserUUID(), x, y, z);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String x, @NotNull String y, @NotNull String z) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat()
                        .replace("<x>", x)
                        .replace("<y>", y)
                        .replace("<z>", z)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
