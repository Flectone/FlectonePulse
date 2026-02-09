package net.flectone.pulse.module.message.format;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.fcolor.FColorModule;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.module.message.format.listener.FormatPulseListener;
import net.flectone.pulse.module.message.format.listener.LegacyColorPulseListener;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.module.message.format.animation.AnimationModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FormatModule extends AbstractModuleLocalization<Localization.Message.Format> {

    private final Map<AdventureTag, TagResolver> tagResolverMap = new EnumMap<>(AdventureTag.class);

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                FColorModule.class,
                FixationModule.class,
                MentionModule.class,
                ModerationModule.class,
                NamesModule.class,
                ObjectModule.class,
                AnimationModule.class,
                QuestionAnswerModule.class,
                ReplacementModule.class,
                ScoreboardModule.class,
                TranslateModule.class,
                WorldModule.class
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();

        putAdventureTag(AdventureTag.HOVER, StandardTags.hoverEvent());
        putAdventureTag(AdventureTag.CLICK, StandardTags.clickEvent());
        putAdventureTag(AdventureTag.COLOR, StandardTags.color());
        putAdventureTag(AdventureTag.KEYBIND, StandardTags.keybind());
        putAdventureTag(AdventureTag.TRANSLATABLE, StandardTags.translatable());
        putAdventureTag(AdventureTag.TRANSLATABLE_FALLBACK, StandardTags.translatableFallback());
        putAdventureTag(AdventureTag.INSERTION, StandardTags.insertion());
        putAdventureTag(AdventureTag.FONT, StandardTags.font());
        putAdventureTag(AdventureTag.DECORATION, StandardTags.decorations());
        putAdventureTag(AdventureTag.GRADIENT, StandardTags.gradient());
        putAdventureTag(AdventureTag.RAINBOW, StandardTags.rainbow());
        putAdventureTag(AdventureTag.RESET, StandardTags.reset());
        putAdventureTag(AdventureTag.NEWLINE, StandardTags.newline());
        putAdventureTag(AdventureTag.TRANSITION, StandardTags.transition());
        putAdventureTag(AdventureTag.SELECTOR, StandardTags.selector());
        putAdventureTag(AdventureTag.SCORE, StandardTags.score());
        putAdventureTag(AdventureTag.NBT, StandardTags.nbt());
        putAdventureTag(AdventureTag.PRIDE, StandardTags.pride());
        putAdventureTag(AdventureTag.SHADOW_COLOR, StandardTags.shadowColor());

        listenerRegistry.register(FormatPulseListener.class);

        if (config().convertLegacyColor()) {
            listenerRegistry.register(LegacyColorPulseListener.class);
        }
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .add(permission().legacyColors())
                .addAll(permission().adventureTags().values());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        tagResolverMap.clear();
    }

    @Override
    public MessageType messageType() {
        return MessageType.FORMAT;
    }

    @Override
    public Message.Format config() {
        return fileFacade.message().format();
    }

    @Override
    public Permission.Message.Format permission() {
        return fileFacade.permission().message().format();
    }

    @Override
    public Localization.Message.Format localization(FEntity sender) {
        return fileFacade.localization(sender).message().format();
    }

    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);

        return messageContext.addTagResolvers(tagResolverMap
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, isUserMessage))
                .map(entry -> {
                    if (entry.getKey() == AdventureTag.GRADIENT
                            && integrationModule.isBedrockPlayer(messageContext.receiver())) {
                        return bedrockGradientTag();
                    }

                    return entry.getValue();
                })
                .toList()
        );
    }

    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!config().adventureTags().contains(adventureTag)) return false;
        if (!tagResolverMap.containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission().adventureTags().get(adventureTag));
    }

    private TagResolver bedrockGradientTag() {
        return TagResolver.resolver( "gradient", (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            TextColor textColor = TextColor.fromHexString(argument.value());
            if (textColor == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.styling(textColor);
        });
    }

    private void putAdventureTag(AdventureTag adventureTag, TagResolver tagResolver) {
        if (config().adventureTags().contains(adventureTag)) {
            tagResolverMap.put(adventureTag, tagResolver);
        }
    }
}
