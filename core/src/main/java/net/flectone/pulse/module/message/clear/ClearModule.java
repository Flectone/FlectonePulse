package net.flectone.pulse.module.message.clear;

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
public class ClearModule extends AbstractModuleMessage<Localization.Message.Clear> {

    private final Message.Clear message;
    private final Permission.Message.Clear permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public ClearModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getClear());

        this.message = fileResolver.getMessage().getClear();
        this.permission = fileResolver.getPermission().getMessage().getClear();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            if (!event.getKey().startsWith("commands.clear.success")) return;

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().size() < 2) return;
            if (!(translatableComponent.args().get(0) instanceof TextComponent numberComponent)) return;
            if (!(translatableComponent.args().get(1) instanceof TextComponent targetComponent)) return;

            String number = numberComponent.content();
            String value = targetComponent.content();

            event.cancel();
            send(event, number, value);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(TranslatableMessageEvent event, String count, String value) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUserUUID());
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        if (event.getKey() == MinecraftTranslationKeys.COMMANDS_CLEAR_SUCCESS_SINGLE) {
            fTarget = fPlayerService.getFPlayer(value);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (event.getKey() == MinecraftTranslationKeys.COMMANDS_CLEAR_SUCCESS_SINGLE
                        ? s.getSingle() : s.getMultiple().replace("<count>", value))
                        .replace("<number>", count)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
