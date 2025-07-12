package net.flectone.pulse.module.message.kill;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.UUID;

@Singleton
public class KillModule extends AbstractModuleMessage<Localization.Message.Kill> {

    private final Message.Kill message;
    private final Permission.Message.Kill permission;
    private final FPlayerService fPlayerService;
    private final EntityUtil entityUtil;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public KillModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      EntityUtil entityUtil,
                      EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getKill());

        this.message = fileResolver.getMessage().getKill();
        this.permission = fileResolver.getPermission().getMessage().getKill();
        this.fPlayerService = fPlayerService;
        this.entityUtil = entityUtil;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(event -> {
            switch (event.getKey()) {
                case COMMANDS_KILL_SUCCESS_MULTIPLE -> handleKillMultiple(event);
                case COMMANDS_KILL_SUCCESS_SINGLE -> handleKillSingle(event);
            }
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key, String value, FEntity fEntity) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FEntity fTarget = fPlayer;

        if (key == MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_SINGLE && fEntity != null) {
            fTarget = fPlayerService.getFPlayer(fEntity.getUuid());

            if (fTarget.isUnknown()) {
                fTarget = fEntity;
            }
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> key == MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_MULTIPLE
                        ? s.getMultiple().replace("<count>", value)
                        : s.getSingle()
                )
                .sound(getSound())
                .sendBuilt();
    }

    private void handleKillMultiple(TranslatableMessageEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();
        if (!(translatableComponent.args().get(0) instanceof TextComponent firstArgument)) return;

        String value = firstArgument.content();

        event.cancel();
        send(event.getUserUUID(), MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_MULTIPLE, value, null);
    }

    private void handleKillSingle(TranslatableMessageEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();

        HoverEvent<?> hoverEvent = null;
        String type = "";
        if (translatableComponent.args().get(0) instanceof TextComponent firstArgument) {
            hoverEvent = firstArgument.hoverEvent();
        } else if (translatableComponent.args().get(0) instanceof TranslatableComponent firstArgument)  {
            hoverEvent = firstArgument.hoverEvent();
            type = firstArgument.key();
        }

        if (hoverEvent == null) return;
        HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();
        if (type.isEmpty()) {
            type = entityUtil.resolveEntityTranslationKey(showEntity.type().key().value());
        }

        String name;
        if (showEntity.name() instanceof TextComponent hoverComponent) {
            name = hoverComponent.content();
        } else if (showEntity.name() instanceof TranslatableComponent hoverComponent) {
            name = hoverComponent.key();
        } else return;

        UUID uuid = showEntity.id();
        FEntity fEntity = new FEntity(name, uuid, type);

        event.cancel();
        send(event.getUserUUID(), MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_SINGLE, "", fEntity);
    }
}
