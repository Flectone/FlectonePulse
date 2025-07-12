package net.flectone.pulse.module.message.op;

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
public class OpModule extends AbstractModuleMessage<Localization.Message.Op> {

    private final Message.Op message;
    private final Permission.Message.Op permission;
    private final FPlayerService fPlayerService;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public OpModule(FileResolver fileResolver,
                    FPlayerService fPlayerService,
                    EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getOp());

        this.message = fileResolver.getMessage().getOp();
        this.permission = fileResolver.getPermission().getMessage().getOp();
        this.fPlayerService = fPlayerService;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            if (event.getKey() != MinecraftTranslationKeys.COMMANDS_OP_SUCCESS) return;

            TranslatableComponent translatableComponent = event.getComponent();
            if (translatableComponent.args().isEmpty()) return;
            if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return;

            event.cancel();
            send(event.getUserUUID(), targetComponent.content());
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String target) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(Localization.Message.Op::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

}
