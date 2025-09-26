package net.flectone.pulse.module.message.advancement;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.advancement.listener.AdvancementPulseListener;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.module.message.advancement.model.AdvancementMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class AdvancementModule extends AbstractModuleLocalization<Localization.Message.Advancement> {

    private final FileResolver fileResolver;
    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public AdvancementModule(FileResolver fileResolver,
                             IntegrationModule integrationModule,
                             MessagePipeline messagePipeline,
                             ListenerRegistry listenerRegistry) {
        super(MessageType.ADVANCEMENT);

        this.fileResolver = fileResolver;
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(AdvancementPulseListener.class);
    }

    @Override
    public Message.Advancement config() {
        return fileResolver.getMessage().getAdvancement();
    }

    @Override
    public Permission.Message.Advancement permission() {
        return fileResolver.getPermission().getMessage().getAdvancement();
    }

    @Override
    public Localization.Message.Advancement localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getAdvancement();
    }

    @Async
    public void sendChatAdvancement(FPlayer fReceiver, MinecraftTranslationKey translationKey, Advancement advancement) {
        if (!(advancement.getTarget() instanceof FPlayer fTarget)) return;
        if (isModuleDisabledFor(fTarget)) return;
        if (!fTarget.equals(fReceiver)) return;

        sendMessage(AdvancementMetadata.<Localization.Message.Advancement>builder()
                .sender(fTarget)
                .format(localization -> switch (translationKey) {
                    case CHAT_TYPE_ADVANCEMENT_TASK -> localization.getFormatTask();
                    case CHAT_TYPE_ADVANCEMENT_GOAL -> localization.getFormatGoal();
                    case CHAT_TYPE_ADVANCEMENT_CHALLENGE -> localization.getFormatChallenge();
                    case CHAT_TYPE_ACHIEVEMENT_TAKEN -> localization.getFormatTaken();
                    default -> "";
                })
                .advancement(advancement)
                .translationKey(translationKey)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .filter(fPlayer -> integrationModule.canSeeVanished(fTarget, fPlayer))
                .tagResolvers(fResolver -> new TagResolver[]{advancementTag(fTarget, fResolver, advancement.getAdvancementComponent())})
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeUTF(translationKey.name());
                    dataOutputStream.writeAsJson(advancement);
                })
                .integration()
                .build()
        );
    }

    @Async
    public void sendCommandAdvancement(FPlayer fPlayer, MinecraftTranslationKey translationKey, Advancement advancement) {
        if (isModuleDisabledFor(fPlayer)) return;

        Message.Advancement.Command command = config().getCommand();
        if (!command.isEnable()) return;

        sendMessage(AdvancementMetadata.<Localization.Message.Advancement>builder()
                .sender(fPlayer)
                .range(command.getRange())
                .destination(command.getDestination())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_ADVANCEMENT_GRANT_MANY_TO_MANY_SUCCESS -> localization.getGrant().getManyToMany();
                            case COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_MANY_SUCCESS -> localization.getRevoke().getManyToMany();
                            case COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS -> localization.getGrant().getManyToOne();
                            case COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS -> localization.getRevoke().getManyToOne();
                            case COMMANDS_ADVANCEMENT_GRANT_ONE_TO_MANY_SUCCESS -> localization.getGrant().getOneToMany();
                            case COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_MANY_SUCCESS -> localization.getRevoke().getOneToMany();
                            case COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS -> localization.getGrant().getOneToOne();
                            case COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS -> localization.getRevoke().getOneToOne();
                            case COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_MANY_SUCCESS -> localization.getGrant().getCriterionToMany();
                            case COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_ONE_SUCCESS -> localization.getGrant().getCriterionToOne();
                            case COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_MANY_SUCCESS -> localization.getRevoke().getCriterionToMany();
                            case COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_ONE_SUCCESS -> localization.getRevoke().getCriterionToOne();
                            default -> "";
                        },
                        new String[]{"<advancements>", "<players>", "<criterion>"},
                        new String[]{StringUtils.defaultString(advancement.getAdvancements()), StringUtils.defaultString(advancement.getPlayers()), StringUtils.defaultString(advancement.getCriterion())}
                ))
                .advancement(advancement)
                .translationKey(translationKey)
                .tagResolvers(fResolver -> new TagResolver[]{
                        advancementTag(fPlayer, fResolver, advancement.getAdvancementComponent()),
                        targetTag(fResolver, advancement.getTarget())
                })
                .sound(getModuleSound())
                .build()
        );
    }

    public TagResolver advancementTag(FEntity sender, FPlayer receiver, Component advancementComponent) {
        String tag = "advancement";
        if (!isEnable()) return empty(tag);

        HoverEvent<?> hoverEvent = advancementComponent.hoverEvent();
        if (hoverEvent == null || !(hoverEvent.value() instanceof Component hoverEventComponent)) return empty(tag);

        boolean isChallenge = NamedTextColor.DARK_PURPLE.equals(hoverEventComponent.color());

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Localization.Message.Advancement localization = localization(receiver);

            String title = isChallenge
                    ? localization.getTag().getChallenge()
                    : localization.getTag().getTask();

            Component componentTag = messagePipeline
                    .builder(sender, receiver, title)
                    .build();

            return Tag.selfClosingInserting(componentTag.replaceText(TextReplacementConfig.builder()
                    .match("<advancement>")
                    .replacement(advancementComponent.hoverEvent(HoverEvent.showText(hoverEventComponent.color(componentTag.color()))))
                    .build()
            ));
        });
    }
}
