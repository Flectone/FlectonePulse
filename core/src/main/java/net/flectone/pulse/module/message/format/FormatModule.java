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
import net.flectone.pulse.module.message.format.image.ImageModule;
import net.flectone.pulse.module.message.format.listener.FormatPulseListener;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.spoiler.SpoilerModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.AdventureTag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.Arrays;
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

        message.getTags().forEach((key, value) -> {
            if (!value.isEnable()) return;

            registerPermission(permission.getTags().get(key));
        });

        registerPermission(permission.getAll());

        putKyoriTag(AdventureTag.HOVER, StandardTags.hoverEvent());
        putKyoriTag(AdventureTag.CLICK, StandardTags.clickEvent());
        putKyoriTag(AdventureTag.COLOR, StandardTags.color());
        putKyoriTag(AdventureTag.KEYBIND, StandardTags.keybind());
        putKyoriTag(AdventureTag.TRANSLATABLE, StandardTags.translatable());
        putKyoriTag(AdventureTag.TRANSLATABLE_FALLBACK, StandardTags.translatableFallback());
        putKyoriTag(AdventureTag.INSERTION, StandardTags.insertion());
        putKyoriTag(AdventureTag.FONT, StandardTags.font());
        putKyoriTag(AdventureTag.DECORATION, StandardTags.decorations());
        putKyoriTag(AdventureTag.GRADIENT, StandardTags.gradient());
        putKyoriTag(AdventureTag.RAINBOW, StandardTags.rainbow());
        putKyoriTag(AdventureTag.RESET, StandardTags.reset());
        putKyoriTag(AdventureTag.NEWLINE, StandardTags.newline());
        putKyoriTag(AdventureTag.TRANSITION, StandardTags.transition());
        putKyoriTag(AdventureTag.SELECTOR, StandardTags.selector());
        putKyoriTag(AdventureTag.SCORE, StandardTags.score());
        putKyoriTag(AdventureTag.NBT, StandardTags.nbt());
        putKyoriTag(AdventureTag.PRIDE, StandardTags.pride());
        putKyoriTag(AdventureTag.SHADOW_COLOR, StandardTags.shadowColor());

        addChildren(FColorModule.class);
        addChildren(FixationModule.class);
        addChildren(ImageModule.class);
        addChildren(MentionModule.class);
        addChildren(ModerationModule.class);
        addChildren(NameModule.class);
        addChildren(QuestionAnswerModule.class);
        addChildren(ReplacementModule.class);
        addChildren(ScoreboardModule.class);
        addChildren(SpoilerModule.class);
        addChildren(TranslateModule.class);
        addChildren(WorldModule.class);

        listenerRegistry.register(FormatPulseListener.class);

        Arrays.asList(
                AdventureTag.SPOILER,
                AdventureTag.BOLD,
                AdventureTag.ITALIC,
                AdventureTag.UNDERLINE,
                AdventureTag.OBFUSCATED,
                AdventureTag.STRIKETHROUGH
        ).forEach(tag -> {
            String trigger = this.message.getTags().get(tag).getTrigger();
            String regex = "(?<!\\\\)" + trigger + "(.*?)(?<!\\\\)" + trigger;
            patternsMap.put(tag, Pattern.compile(regex));
        });

        patternsMap.put(AdventureTag.IMAGE, Pattern.compile(message.getTags().get(AdventureTag.IMAGE).getTrigger()));
        patternsMap.put(AdventureTag.URL, Pattern.compile(message.getTags().get(AdventureTag.IMAGE).getTrigger()));
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

    private void putKyoriTag(AdventureTag type, TagResolver tagResolver) {
        Message.Format.Tag tag = message.getTags().get(type);
        if (tag == null) return;
        if (!tag.isEnable()) return;

        tagResolverMap.put(type, tagResolver);
    }
}
