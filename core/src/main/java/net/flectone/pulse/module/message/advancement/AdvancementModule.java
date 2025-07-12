package net.flectone.pulse.module.message.advancement;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class AdvancementModule extends AbstractModuleMessage<Localization.Message.Advancement> {

    @Getter private final Message.Advancement message;
    private final Permission.Message.Advancement permission;
    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final Gson gson;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public AdvancementModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             IntegrationModule integrationModule,
                             MessagePipeline messagePipeline,
                             Gson gson,
                             EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getAdvancement());

        this.message = fileResolver.getMessage().getAdvancement();
        this.permission = fileResolver.getPermission().getMessage().getAdvancement();
        this.fPlayerService = fPlayerService;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
        this.gson = gson;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        eventProcessRegistry.registerMessageHandler(translatableMessageEvent -> {
            switch (translatableMessageEvent.getKey()) {
                case CHAT_TYPE_ADVANCEMENT_TASK, CHAT_TYPE_ADVANCEMENT_GOAL, CHAT_TYPE_ADVANCEMENT_CHALLENGE ->
                        processAdvancement(translatableMessageEvent);
                case COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS,
                     COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS ->
                        processCommand(translatableMessageEvent);
            }
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID uuid, String target, Advancement advancement) {
        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;
        if (checkModulePredicates(fTarget)) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(uuid);
        if (!fTarget.equals(fReceiver)) return;

        builder(fTarget)
                .range(message.getRange())
                .destination(message.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.ADVANCEMENT))
                .filter(fPlayer -> integrationModule.isVanishedVisible(fTarget, fPlayer))
                .tag(MessageTag.ADVANCEMENT)
                .format(s -> convert(s, advancement))
                .tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fResolver, advancement)})
                .proxy(output -> output.writeUTF(gson.toJson(advancement)))
                .integration()
                .sound(getSound())
                .sendBuilt();
    }

    @Async
    public void send(Relation relation,
                     boolean revoke,
                     UUID uuid,
                     String target,
                     @Nullable Advancement advancement,
                     @Nullable String content) {
        if (advancement == null && content == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        Builder builder = builder(fTarget)
                .receiver(fPlayer)
                .format(s -> {
                    Localization.Message.Advancement.Command subcommand = revoke ? s.getRevoke() : s.getGrant();

                    return switch (relation) {
                        case MANY_TO_ONE -> subcommand.getManyToOne().replace("<number>", content);
                        case ONE_TO_ONE_TEXT -> subcommand.getOneToOne().replace("<advancement>", content);
                        case ONE_TO_ONE_ADVANCEMENT -> subcommand.getOneToOne();
                    };
                })
                .sound(getSound());

        if (relation == Relation.ONE_TO_ONE_ADVANCEMENT && advancement != null) {
            builder.tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fPlayer, advancement)});
        }

        builder.sendBuilt();
    }

    public String convert(Localization.Message.Advancement message, Advancement advancement) {
        String string = switch (advancement.type()) {
            case CHAT_TYPE_ADVANCEMENT_TASK -> message.getTask().getFormat();
            case CHAT_TYPE_ADVANCEMENT_GOAL -> message.getGoal().getFormat();
            case CHAT_TYPE_ADVANCEMENT_CHALLENGE -> message.getChallenge().getFormat();
            default -> "";
        };

        return string
                .replace("<title>", advancement.title())
                .replace("<description>", advancement.description());
    }

    public TagResolver advancementTag(FEntity sender, FPlayer receiver, @NotNull Advancement advancement) {
        String tag = "advancement";
        if (!isEnable()) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Localization.Message.Advancement localization = resolveLocalization(receiver);

            String title = switch (advancement.type()) {
                case CHAT_TYPE_ADVANCEMENT_TASK -> localization.getTask().getTag();
                case CHAT_TYPE_ADVANCEMENT_GOAL -> localization.getGoal().getTag();
                case CHAT_TYPE_ADVANCEMENT_CHALLENGE -> localization.getChallenge().getTag();
                default -> "";
            };

            Component component = messagePipeline.builder(sender, receiver, title
                            .replace("<title>", advancement.title())
                            .replace("<description>", advancement.description())
                    )
                    .build();

            return Tag.inserting(component);
        });
    }

    private void processAdvancement(TranslatableMessageEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();
        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 2) return;
        if (!(translationArguments.get(0) instanceof TextComponent targetComponent)) return;
        String target = targetComponent.content();

        if (!(translationArguments.get(1) instanceof TranslatableComponent titleComponent)) return;
        if (titleComponent.args().isEmpty()) return;

        Advancement advancement;
        if (titleComponent.args().get(0) instanceof TranslatableComponent title) {

            HoverEvent<?> hoverEvent = title.hoverEvent();

            if (hoverEvent == null) return;
            if (!(hoverEvent.value() instanceof Component descriptionComponent)) return;
            if (descriptionComponent.children().size() < 2) return;
            if (!(descriptionComponent.children().get(1) instanceof TranslatableComponent component)) return;

            String titleKey = title.key();
            String descriptionKey = component.key();
            advancement = new Advancement(titleKey, descriptionKey, event.getKey());

        } else if (titleComponent.args().get(0) instanceof TextComponent title) {

            HoverEvent<?> hoverEvent = title.hoverEvent();

            if (hoverEvent == null) return;
            if (!(hoverEvent.value() instanceof TextComponent descriptionComponent)) return;
            if (descriptionComponent.children().size() < 2) return;
            if (!(descriptionComponent.children().get(1) instanceof TextComponent childrenComponent)) return;

            String titleKey = title.content();
            String descriptionKey = childrenComponent.content();
            advancement = new Advancement(titleKey, descriptionKey, event.getKey());
        } else return;

        event.cancel();
        send(event.getUserUUID(), target, advancement);
    }

    private void processCommand(TranslatableMessageEvent event) {
        MinecraftTranslationKeys type = event.getKey();

        boolean revoke = type == MinecraftTranslationKeys.COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS
                || type == MinecraftTranslationKeys.COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS;
        if (revoke && !message.isRevoke()) return;
        if (!revoke && !message.isGrant()) return;

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().size() < 2) return;

        Component argument = translatableComponent.args().get(0);
        Component playerArgument = translatableComponent.args().get(1);

        if (!(playerArgument instanceof TextComponent playerComponent)) return;

        String target = playerComponent.content();

        String content = null;
        Advancement advancement = null;
        Relation relation;

        switch (type) {
            case COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS -> {
                if (argument instanceof TranslatableComponent argumentIn) {
                    if (argumentIn.args().isEmpty()) return;
                    if (argumentIn.args().get(0) instanceof TranslatableComponent titleComponent) {

                        HoverEvent<?> hoverEvent = titleComponent.hoverEvent();

                        if (hoverEvent == null) return;
                        if (!(hoverEvent.value() instanceof TranslatableComponent description)) return;
                        if (description.children().size() < 2) return;
                        if (!(description.children().get(1) instanceof TranslatableComponent childrenComponent)) return;

                        String titleKey = titleComponent.key();
                        String descriptionKey = childrenComponent.key();

                        MinecraftTranslationKeys advancementType = NamedTextColor.DARK_PURPLE.equals(description.color())
                                ? MinecraftTranslationKeys.CHAT_TYPE_ADVANCEMENT_CHALLENGE
                                : MinecraftTranslationKeys.CHAT_TYPE_ADVANCEMENT_TASK;

                        advancement = new Advancement(titleKey, descriptionKey, advancementType);

                    } else if (argumentIn.args().get(0) instanceof TextComponent titleComponent) {

                        HoverEvent<?> hoverEvent = titleComponent.hoverEvent();

                        if (hoverEvent == null) return;
                        if (!(hoverEvent.value() instanceof TextComponent descriptionComponent)) return;
                        if (descriptionComponent.children().size() < 2) return;
                        if (!(descriptionComponent.children().get(1) instanceof TextComponent childrenComponent)) return;

                        String title = titleComponent.content();
                        String description = childrenComponent.content();

                        MinecraftTranslationKeys advancementType = NamedTextColor.DARK_PURPLE.equals(descriptionComponent.color())
                                ? MinecraftTranslationKeys.CHAT_TYPE_ADVANCEMENT_CHALLENGE
                                : MinecraftTranslationKeys.CHAT_TYPE_ADVANCEMENT_TASK;

                        advancement = new Advancement(title, description, advancementType);
                    } else return;

                    relation = Relation.ONE_TO_ONE_ADVANCEMENT;
                } else if (argument instanceof TextComponent textComponent){
                    content = textComponent.content();
                    relation = Relation.ONE_TO_ONE_TEXT;
                } else return;
            }
            case COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS -> {
                if (!(argument instanceof TextComponent textComponent)) return;

                content = textComponent.content();
                relation = Relation.MANY_TO_ONE;
            }
            default -> {
                return;
            }
        }

        event.cancel();
        send(relation, revoke, event.getUserUUID(), target, advancement, content);
    }

    public enum Relation {
        MANY_TO_ONE,
        ONE_TO_ONE_ADVANCEMENT,
        ONE_TO_ONE_TEXT
    }
}
