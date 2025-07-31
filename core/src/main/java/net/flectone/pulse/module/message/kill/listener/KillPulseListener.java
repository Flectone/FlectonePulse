package net.flectone.pulse.module.message.kill.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.kill.KillModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.incendo.cloud.type.tuple.Triplet;

import java.util.UUID;

@Singleton
public class KillPulseListener implements PulseListener {

    private final KillModule killModule;
    private final EntityUtil entityUtil;
    private final FPlayerService fPlayerService;

    @Inject
    public KillPulseListener(KillModule killModule,
                             EntityUtil entityUtil,
                             FPlayerService fPlayerService) {
        this.killModule = killModule;
        this.entityUtil = entityUtil;
        this.fPlayerService = fPlayerService;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        switch (event.getKey()) {
            case COMMANDS_KILL_SUCCESS_MULTIPLE -> handleKillMultiple(event);
            case COMMANDS_KILL_SUCCESS_SINGLE, COMMANDS_KILL_SUCCESS -> handleKillSingle(event);
        }
    }

    private void handleKillMultiple(TranslatableMessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();
        if (!(translatableComponent.args().get(0) instanceof TextComponent firstArgument)) return;

        String value = firstArgument.content();

        event.cancelPacket();
        killModule.send(event.getFPlayer(), MinecraftTranslationKey.COMMANDS_KILL_SUCCESS_MULTIPLE, value, null);
    }

    private void handleKillSingle(TranslatableMessageReceiveEvent event) {
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

        event.cancelPacket();
        killModule.send(event.getFPlayer(), event.getKey(), "", new FEntity(name, uuid, type));
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
