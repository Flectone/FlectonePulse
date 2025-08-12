package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.fcolor.FColorModule;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.module.message.format.listener.FormatPulseListener;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.AdventureTag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

@Singleton
public class FormatModule extends AbstractModuleLocalization<Localization.Message.Format> {

    @Getter private final Map<AdventureTag, TagResolver> tagResolverMap = new EnumMap<>(AdventureTag.class);
    @Getter private final Map<AdventureTag, Pattern> patternsMap = new EnumMap<>(AdventureTag.class);

    private final Message.Format message;
    private final Permission.Message.Format permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FormatModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat());

        this.message = fileResolver.getMessage().getFormat();
        this.permission = fileResolver.getPermission().getMessage().getFormat();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        message.getAdventureTags().forEach(adventureTag -> registerPermission(permission.getAdventureTags().get(adventureTag)));

        registerPermission(permission.getAll());

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

        addChildren(FColorModule.class);
        addChildren(FixationModule.class);
        addChildren(MentionModule.class);
        addChildren(ModerationModule.class);
        addChildren(NameModule.class);
        addChildren(QuestionAnswerModule.class);
        addChildren(ReplacementModule.class);
        addChildren(ScoreboardModule.class);
        addChildren(TranslateModule.class);
        addChildren(WorldModule.class);

        listenerRegistry.register(FormatPulseListener.class);
    }

    @Override
    public void onDisable() {
        tagResolverMap.clear();
        patternsMap.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    private void putAdventureTag(AdventureTag adventureTag, TagResolver tagResolver) {
        if (message.getAdventureTags().contains(adventureTag)) {
            tagResolverMap.put(adventureTag, tagResolver);
        }
    }
}
