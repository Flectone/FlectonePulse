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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.incendo.cloud.type.tuple.Triplet;

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
                case COMMANDS_KILL_SUCCESS_SINGLE, COMMANDS_KILL_SUCCESS -> handleKillSingle(event);
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

        boolean isSingle = key == MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS_SINGLE
                || key == MinecraftTranslationKeys.COMMANDS_KILL_SUCCESS;

        if (isSingle && fEntity != null && fEntity.getUuid() != null) {
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
        if (translatableComponent.args().isEmpty()) return;

        Component firstArgument = translatableComponent.args().get(0);
        UUID uuid = null;

        String content = switch (firstArgument) {
            case TranslatableComponent translatableArg -> {
                String insertion = translatableArg.insertion();
                if (insertion != null && !insertion.isEmpty()) {
                    try {
                        uuid = UUID.fromString(insertion);
                    } catch (IllegalArgumentException e) {
                        // invalid UUID
                    }
                }

                yield translatableArg.key();
            }
            case TextComponent textArg -> textArg.content();
            default -> null;
        };

        if (content == null) return;

        Triplet<String, String, UUID> triplet = processHoverKillComponent(content, content, uuid, firstArgument.hoverEvent());
        String name = triplet.first();
        if (name.isEmpty()) return;

        String type = triplet.second();
        uuid = triplet.third();

        event.cancel();
        send(event.getUserUUID(), event.getKey(), "", new FEntity(name, uuid, type));
    }

    private Triplet<String, String, UUID> processHoverKillComponent(String name, String type, UUID uuid, HoverEvent<?> hoverEvent) {
        if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();
            uuid = showEntity.id();
            type = entityUtil.resolveEntityTranslationKey(showEntity.type().key().value());
            if (showEntity.name() instanceof TextComponent hoverText) {
                name = hoverText.content();
            } else if (showEntity.name() instanceof TranslatableComponent hoverTranslatable) {
                name = hoverTranslatable.key();
            }
        } else if (uuid == null) {
            uuid = fPlayerService.getFPlayer(name).getUuid();
        }

        return Triplet.of(name, type, uuid);
    }
}
