package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.fcolor.FColorModule;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.module.message.format.listener.FormatPulseListener;
import net.flectone.pulse.module.message.format.listener.LegacyColorPulseListener;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.module.message.format.object.ObjectModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.EnumMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FormatModule extends AbstractModuleLocalization<Localization.Message.Format> {

    private final Map<AdventureTag, TagResolver> tagResolverMap = new EnumMap<>(AdventureTag.class);

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChildren(FColorModule.class);
        addChildren(FixationModule.class);
        addChildren(MentionModule.class);
        addChildren(ModerationModule.class);
        addChildren(NamesModule.class);
        addChildren(ObjectModule.class);
        addChildren(QuestionAnswerModule.class);
        addChildren(ReplacementModule.class);
        addChildren(ScoreboardModule.class);
        addChildren(TranslateModule.class);
        addChildren(WorldModule.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(permission().getLegacyColors());

        config().getAdventureTags().forEach(adventureTag -> registerPermission(permission().getAdventureTags().get(adventureTag)));

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

        if (config().isConvertLegacyColor()) {
            listenerRegistry.register(LegacyColorPulseListener.class);
        }
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
        return fileResolver.getMessage().getFormat();
    }

    @Override
    public Permission.Message.Format permission() {
        return fileResolver.getPermission().getMessage().getFormat();
    }

    @Override
    public Localization.Message.Format localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFormat();
    }

    public void addTags(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);

        tagResolverMap
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, isUserMessage))
                .forEach(entry -> messageContext.addReplacementTag(entry.getValue()));
    }

    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!config().getAdventureTags().contains(adventureTag)) return false;
        if (!tagResolverMap.containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission().getAdventureTags().get(adventureTag));
    }

    private void putAdventureTag(AdventureTag adventureTag, TagResolver tagResolver) {
        if (config().getAdventureTags().contains(adventureTag)) {
            tagResolverMap.put(adventureTag, tagResolver);
        }
    }
}
