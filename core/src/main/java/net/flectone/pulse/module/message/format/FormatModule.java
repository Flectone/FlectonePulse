package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.formatter.ItemTextFormatter;
import net.flectone.pulse.formatter.MessageFormatter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.format.color.ColorModule;
import net.flectone.pulse.module.message.format.emoji.EmojiModule;
import net.flectone.pulse.module.message.format.fixation.FixationModule;
import net.flectone.pulse.module.message.format.image.ImageModule;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.module.message.format.questionanswer.QuestionAnswerModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.spoiler.SpoilerModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.HashMap;
import java.util.Map;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class FormatModule extends AbstractModuleMessage<Localization.Message.Format> {

    @Getter private final Map<TagType, TagResolver> tagResolverMap = new HashMap<>();

    private final Message.Format message;
    @Getter private final Permission.Message.Format permission;

    private final PlatformServerAdapter platformServerAdapter;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SkinService skinService;
    private final PermissionChecker permissionChecker;
    private final ItemTextFormatter itemTextFormatter;
    private final MessageFormatter messageFormatter;

    @Inject
    public FormatModule(FileManager fileManager,
                        PlatformServerAdapter platformServerAdapter,
                        FPlayerService fPlayerService,
                        PlatformPlayerAdapter platformPlayerAdapter,
                        SkinService skinService,
                        PermissionChecker permissionChecker,
                        ItemTextFormatter itemTextFormatter,
                        MessageFormatter messageFormatter) {
        super(localization -> localization.getMessage().getFormat());

        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.skinService = skinService;
        this.permissionChecker = permissionChecker;
        this.itemTextFormatter = itemTextFormatter;
        this.messageFormatter = messageFormatter;

        message = fileManager.getMessage().getFormat();
        permission = fileManager.getPermission().getMessage().getFormat();

        addChildren(ColorModule.class);
        addChildren(EmojiModule.class);
        addChildren(FixationModule.class);
        addChildren(ImageModule.class);
        addChildren(MentionModule.class);
        addChildren(ModerationModule.class);
        addChildren(NameModule.class);
        addChildren(QuestionAnswerModule.class);
        addChildren(ScoreboardModule.class);
        addChildren(SpoilerModule.class);
        addChildren(TranslateModule.class);
        addChildren(WorldModule.class);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        message.getTags().forEach((key, value) -> {
            if (!value.isEnable()) return;

            registerPermission(permission.getTags().get(key));
        });

        registerPermission(permission.getAll());

        putKyoriTag(TagType.HOVER, StandardTags.hoverEvent());
        putKyoriTag(TagType.CLICK, StandardTags.clickEvent());
        putKyoriTag(TagType.COLOR, StandardTags.color());
        putKyoriTag(TagType.KEYBIND, StandardTags.keybind());
        putKyoriTag(TagType.TRANSLATABLE, StandardTags.translatable());
        putKyoriTag(TagType.TRANSLATABLE_FALLBACK, StandardTags.translatableFallback());
        putKyoriTag(TagType.INSERTION, StandardTags.insertion());
        putKyoriTag(TagType.FONT, StandardTags.font());
        putKyoriTag(TagType.DECORATION, StandardTags.decorations());
        putKyoriTag(TagType.GRADIENT, StandardTags.gradient());
        putKyoriTag(TagType.RAINBOW, StandardTags.rainbow());
        putKyoriTag(TagType.RESET, StandardTags.reset());
        putKyoriTag(TagType.NEWLINE, StandardTags.newline());
        putKyoriTag(TagType.TRANSITION, StandardTags.transition());
        putKyoriTag(TagType.SELECTOR, StandardTags.selector());
        putKyoriTag(TagType.SCORE, StandardTags.score());
        putKyoriTag(TagType.NBT, StandardTags.nbt());
        putKyoriTag(TagType.PRIDE, StandardTags.pride());
        putKyoriTag(TagType.SHADOW_COLOR, StandardTags.shadowColor());
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    private void putKyoriTag(TagType type, TagResolver tagResolver) {
        Message.Format.Tag tag = message.getTags().get(type);
        if (tag == null) return;
        if (!tag.isEnable()) return;

        tagResolverMap.put(type, tagResolver);
    }

    public TagResolver tpsTag(FEntity sender, FEntity fReceiver) {
        String tag = "tps";
        if (!isCorrectTag(TagType.TPS, sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.TPS).replace("<tps>", String.valueOf(platformServerAdapter.getTPS()));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver onlineTag(FEntity sender, FEntity fReceiver) {
        String tag = "online";
        if (!isCorrectTag(TagType.ONLINE, sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.ONLINE).replace("<online>", String.valueOf(platformServerAdapter.getOnlineCount()));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver pingTag(FEntity sender, FEntity fReceiver) {
        String tag = "ping";
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);
        if (!isCorrectTag(TagType.PING, sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            int ping = fPlayerService.getPing(fPlayer);
            String string = resolveLocalization(fReceiver).getTags().get(TagType.PING).replace("<ping>", String.valueOf(ping));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver coordsTag(FEntity sender, FEntity fReceiver) {
        String tag = "coords";
        if (!isCorrectTag(TagType.COORDS, sender)) return emptyTagResolver(tag);

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(sender);
        if (coordinates == null) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.COORDS)
                    .replace("<x>", String.valueOf(coordinates.x()))
                    .replace("<y>", String.valueOf(coordinates.y()))
                    .replace("<z>", String.valueOf(coordinates.z()));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver statsTag(FEntity sender, FEntity fReceiver) {
        String tag = "stats";
        if (!isCorrectTag(TagType.STATS, sender)) return emptyTagResolver(tag);

        PlatformPlayerAdapter.Statistics statistics = platformPlayerAdapter.getStatistics(sender);
        if (statistics == null) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.STATS)
                    .replace("<hp>", String.valueOf(statistics.health()))
                    .replace("<armor>", String.valueOf(statistics.armor()))
                    .replace("<exp>", String.valueOf(statistics.level()))
                    .replace("<food>", String.valueOf(statistics.food())
                    .replace("<attack>", String.valueOf(statistics.damage())));

            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver skinTag(FEntity sender, FEntity fReceiver) {
        String tag = "skin";
        if (!isCorrectTag(TagType.SKIN, sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {

            String url = skinService.getBodyUrl(sender);
            String string = resolveLocalization(fReceiver).getTags().get(TagType.SKIN).replace("<message>", url);
            Component component = messageFormatter.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver itemTag(FEntity sender, FEntity fReceiver) {
        String tag = "item";
        if (!isCorrectTag(TagType.ITEM, sender)) return emptyTagResolver(tag);

        Object itemStackObject = platformPlayerAdapter.getItem(sender.getUuid());

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.ITEM);
            return Tag.selfClosingInserting(messageFormatter.builder(sender, fReceiver, string)
                    .build()
                    .replaceText(TextReplacementConfig.builder()
                            .match("<message>")
                            .replacement(itemTextFormatter.translatableComponent(itemStackObject))
                            .build()
                    )
            );
        });
    }

    public TagResolver urlTag(FEntity sender, FEntity fReceiver) {
        String tag = "url";
        if (!isCorrectTag(TagType.URL, sender)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {

            Tag.Argument urlArgument = argumentQueue.peek();
            if (urlArgument == null) return Tag.selfClosingInserting(Component.empty());

            String url = urlArgument.value();
            Component component = messageFormatter.builder(sender, fReceiver, resolveLocalization(fReceiver).getTags().get(TagType.URL).replace("<message>", url))
                    .url(false)
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    public String replaceAll(FEntity sender, FEntity fReceiver, String message) {
        if (checkModulePredicates(sender)) return message;

        if (isCorrectTag(TagType.IMAGE, sender)) {
            Localization.Message.Format localization = resolveLocalization(fReceiver);
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.IMAGE),
                    this.message.getTags().get(TagType.IMAGE).getTrigger(),
                    localization.getTags().get(TagType.IMAGE).replace("<message>", "$1")
            );
        }

        if (isCorrectTag(TagType.URL, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.URL),
                    this.message.getTags().get(TagType.URL).getTrigger(),
                    "<url:'$1'>"
            );
        }

        if (isCorrectTag(TagType.PING, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.PING).getTrigger(), "<ping>");
        }

        if (isCorrectTag(TagType.TPS, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.TPS).getTrigger(), "<tps>");
        }

        if (isCorrectTag(TagType.ONLINE, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.ONLINE).getTrigger(), "<online>");
        }

        if (isCorrectTag(TagType.COORDS, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.COORDS).getTrigger(), "<coords>");
        }

        if (isCorrectTag(TagType.STATS, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.STATS).getTrigger(), "<stats>");
        }

        if (isCorrectTag(TagType.SKIN, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.SKIN).getTrigger(), "<skin>");
        }

        if (isCorrectTag(TagType.ITEM, sender)) {
            message = message
                    .replace(this.message.getTags().get(TagType.ITEM).getTrigger(), "<item>");
        }

        String regex = "<trigger>(.*?)<trigger>";

        if (isCorrectTag(TagType.SPOILER, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.SPOILER),
                    regex.replace("<trigger>", this.message.getTags().get(TagType.SPOILER).getTrigger()),
                    "<spoiler:'$1'>"
            );
        }

        if (isCorrectTag(TagType.BOLD, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.BOLD),
                    regex.replace("<trigger>", this.message.getTags().get(TagType.BOLD).getTrigger()),
                    "<bold>$1</bold>"
            );
        }

        if (isCorrectTag(TagType.ITALIC, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.ITALIC),
                    regex.replace("<trigger>", this.message.getTags().get(TagType.ITALIC).getTrigger()),
                    "<italic>$1</italic>"
            );
        }

        if (isCorrectTag(TagType.UNDERLINE, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.UNDERLINE),
                    regex.replace("<trigger>", this.message.getTags().get(TagType.UNDERLINE).getTrigger()),
                    "<underlined>$1</underlined>"
            );
        }

        if (isCorrectTag(TagType.OBFUSCATED, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.OBFUSCATED),
                    regex.replace("<trigger>", this.message.getTags().get(TagType.OBFUSCATED).getTrigger()),
                    "<obfuscated>$1</obfuscated>"
            );
        }

        if (isCorrectTag(TagType.STRIKETHROUGH, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(TagType.STRIKETHROUGH),
                    regex.replace("<trigger>", this.message.getTags().get(TagType.STRIKETHROUGH).getTrigger()),
                    "<strikethrough>$1</strikethrough>"
            );
        }

        return message;
    }

    public String replaceAll(FEntity sender, String message, Permission.PermissionEntry permission, String trigger, String format) {
        if (checkModulePredicates(sender)) return message;
        if (!permissionChecker.check(sender, permission)) return message;

        return message.replaceAll(trigger, format);
    }

    public boolean isCorrectTag(TagType tagType, FEntity sender, boolean needPermission) {
        if (checkModulePredicates(sender)) return false;
        if (!message.getTags().get(tagType).isEnable()) return false;
        if (!tagResolverMap.containsKey(tagType)) return false;

        return !needPermission || permissionChecker.check(sender, permission.getTags().get(tagType));
    }

    public boolean isCorrectTag(TagType tagType, FEntity sender) {
        if (checkModulePredicates(sender)) return false;
        if (!message.getTags().get(tagType).isEnable()) return false;

        return permissionChecker.check(sender, permission.getTags().get(tagType));
    }
}
