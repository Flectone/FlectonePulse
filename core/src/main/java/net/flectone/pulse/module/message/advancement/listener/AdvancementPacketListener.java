package net.flectone.pulse.module.message.advancement.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.util.AdvancementType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.UUID;

@Singleton
public class AdvancementPacketListener extends AbstractPacketListener {

    private final AdvancementModule advancementModule;

    @Inject
    public AdvancementPacketListener(AdvancementModule advancementModule) {
        this.advancementModule = advancementModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;
        if (!advancementModule.isEnable()) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;

        if (key.startsWith("commands.advancement")) {
            processCommand(key, translatableComponent, event);
            return;
        }

        if (key.startsWith("chat.type.advancement")) {
            processAdvancement(key, translatableComponent, event);
        }
    }

    private void processAdvancement(String key, TranslatableComponent translatableComponent, PacketSendEvent event) {
        String stringType = key.substring(key.lastIndexOf(".") + 1);
        AdvancementType type = AdvancementType.fromString(stringType);
        if (type == null) return;

        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 2) return;
        if (!(translationArguments.get(0) instanceof TextComponent targetComponent)) return;
        String target = targetComponent.content();

        if (!(translationArguments.get(1) instanceof TranslatableComponent titleComponent)) return;
        if (titleComponent.args().isEmpty()) return;

        Advancement advancement;
        if (titleComponent.args().get(0) instanceof TranslatableComponent title) {
            String titleKey = title.key();
            String descriptionKey = titleKey.substring(0, titleKey.lastIndexOf(".") + 1) + "description";
            advancement = new Advancement(titleKey, descriptionKey, type);
        } else if (titleComponent.args().get(0) instanceof TextComponent title) {

            HoverEvent<?> hoverEvent = title.hoverEvent();

            if (hoverEvent == null) return;
            if (!(hoverEvent.value() instanceof TextComponent descriptionComponent)) return;
            if (descriptionComponent.children().size() < 2) return;
            if (!(descriptionComponent.children().get(1) instanceof TextComponent childrenComponent)) return;

            String titleKey = title.content();
            String descriptionKey = childrenComponent.content();
            advancement = new Advancement(titleKey, descriptionKey, type);
        } else return;

        UUID uuid = event.getUser().getUUID();

        event.setCancelled(true);
        advancementModule.send(uuid, target, advancement);
    }

    private void processCommand(String key, TranslatableComponent translatableComponent, PacketSendEvent event) {
        boolean revoke = key.startsWith("commands.advancement.revoke");
        if (revoke && !advancementModule.getMessage().isRevoke()) return;
        if (!revoke && !advancementModule.getMessage().isGrant()) return;
        if (translatableComponent.args().size() < 2) return;

        Component argument = translatableComponent.args().get(0);
        Component playerArgument = translatableComponent.args().get(1);

        if (!(playerArgument instanceof TextComponent playerComponent)) return;

        String target = playerComponent.content();
        UUID uuid = event.getUser().getUUID();

        String content = null;
        Advancement advancement = null;
        AdvancementModule.Relation relation;

        switch (key) {
            case "commands.advancement.revoke.one.to.one.success", "commands.advancement.grant.one.to.one.success" -> {
                if (argument instanceof TranslatableComponent argumentIn) {
                    if (argumentIn.args().isEmpty()) return;
                    if (argumentIn.args().get(0) instanceof TranslatableComponent titleComponent) {
                        String titleKey = titleComponent.key();
                        String descriptionKey = titleKey.substring(0, titleKey.lastIndexOf(".") + 1) + "description";

                        HoverEvent<?> hoverEvent = titleComponent.hoverEvent();

                        if (hoverEvent == null) return;
                        if (!(hoverEvent.value() instanceof TranslatableComponent description)) return;

                        AdvancementType type = NamedTextColor.DARK_PURPLE.equals(description.color())
                                ? AdvancementType.CHALLENGE
                                : AdvancementType.TASK;

                        advancement = new Advancement(titleKey, descriptionKey, type);

                    } else if (argumentIn.args().get(0) instanceof TextComponent titleComponent) {

                        HoverEvent<?> hoverEvent = titleComponent.hoverEvent();

                        if (hoverEvent == null) return;
                        if (!(hoverEvent.value() instanceof TextComponent descriptionComponent)) return;
                        if (descriptionComponent.children().size() < 2) return;
                        if (!(descriptionComponent.children().get(1) instanceof TextComponent childrenComponent)) return;

                        String title = titleComponent.content();
                        String description = childrenComponent.content();

                        AdvancementType type = NamedTextColor.DARK_PURPLE.equals(descriptionComponent.color())
                                ? AdvancementType.CHALLENGE
                                : AdvancementType.TASK;

                        advancement = new Advancement(title, description, type);
                    } else return;

                    relation = AdvancementModule.Relation.ONE_TO_ONE_ADVANCEMENT;
                } else if (argument instanceof TextComponent textComponent){
                    content = textComponent.content();
                    relation = AdvancementModule.Relation.ONE_TO_ONE_TEXT;
                } else return;
            }
            case "commands.advancement.revoke.many.to.one.success", "commands.advancement.grant.many.to.one.success" -> {
                if (!(argument instanceof TextComponent textComponent)) return;

                content = textComponent.content();
                relation = AdvancementModule.Relation.MANY_TO_ONE;
            }
            default -> {
                return;
            }
        }

        event.setCancelled(true);
        advancementModule.send(relation, revoke, uuid, target, advancement, content);
    }
}
