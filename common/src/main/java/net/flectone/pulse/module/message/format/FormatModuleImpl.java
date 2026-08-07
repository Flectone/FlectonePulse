package net.flectone.pulse.module.message.format;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.animation.AnimationModule;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.module.message.format.fading.FadingModule;
import net.flectone.pulse.module.message.format.fcolor.FColorModule;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.module.message.format.listener.PulseFormatListener;
import net.flectone.pulse.module.message.format.listener.PulseLegacyColorListener;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.module.message.format.padding.PaddingModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FormatModuleImpl implements FormatModule {

    private final Map<AdventureTag, TagResolver> tagResolverMap = new EnumMap<>(AdventureTag.class);

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final IntegrationModule integrationModule;
    private final ModuleController moduleController;
    private final SocialService socialService;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(FormatModule.super.children());
        children.add(AnimationModule.class);
        children.add(ConditionModule.class);
        children.add(FadingModule.class);
        children.add(FColorModule.class);
        children.add(FixationModule.class);
        children.add(MentionModule.class);
        children.add(ModerationModule.class);
        children.add(NamesModule.class);
        children.add(ObjectModule.class);
        children.add(PaddingModule.class);
        children.add(QuestionAnswerModule.class);
        children.add(ReplacementModule.class);
        children.add(TranslateModule.class);
        children.add(WorldModule.class);
        return Collections.unmodifiableSet(children);
    }

    @Override
    public void onEnable() {
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

        listenerRegistry.register(PulseFormatListener.class);

        if (config().convertLegacyColor()) {
            listenerRegistry.register(PulseLegacyColorListener.class);
        }
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(FormatModule.super.permissions());
        permissions.add(permission().legacyColors());
        permissions.addAll(permission().adventureTags().values());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void onDisable() {
        tagResolverMap.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT;
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
    public Localization.Message.Format localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format();
    }

    @Override
    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.PLAYER_MESSAGE);

        if (sender instanceof FPlayer fPlayer && !isUserMessage) {
            messageContext = messageContext.addTagResolver(
                    Placeholder.unparsed("server", StringUtils.defaultString(socialService.getSetting(fPlayer, SettingText.SERVER)))
            );
        }

        FPlayer fReceiver = messageContext.receiver();
        return messageContext.addTagResolvers(tagResolverMap
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, isUserMessage))
                .map(entry -> {
                    if (entry.getKey() == AdventureTag.GRADIENT
                            && integrationModule.isBedrockPlayer(fReceiver)) {
                        return bedrockGradientTag();
                    }

                    return entry.getValue();
                })
                .toArray(TagResolver[]::new)
        );
    }

    @Override
    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!config().adventureTags().contains(adventureTag)) return false;
        if (!tagResolverMap.containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission().adventureTags().get(adventureTag));
    }

    private TagResolver bedrockGradientTag() {
        return TagResolver.resolver("gradient", (argumentQueue, _) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return MessagePipeline.ReplacementTag.emptyTag();

            TextColor textColor = TextColor.fromHexString(argument.value());
            if (textColor == null) return MessagePipeline.ReplacementTag.emptyTag();

            return Tag.styling(textColor);
        });
    }

    private void putAdventureTag(AdventureTag adventureTag, TagResolver tagResolver) {
        if (config().adventureTags().contains(adventureTag)) {
            tagResolverMap.put(adventureTag, tagResolver);
        }
    }
}
