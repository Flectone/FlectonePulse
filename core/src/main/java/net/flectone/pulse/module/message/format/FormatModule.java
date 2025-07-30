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
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.converter.LegacyMiniConvertor;
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
import net.flectone.pulse.module.message.format.style.StyleModule;
import net.flectone.pulse.module.message.format.translate.TranslateModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.constant.AdventureTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

@Singleton
public class FormatModule extends AbstractModuleMessage<Localization.Message.Format> implements MessageProcessor {

    @Getter private final Map<AdventureTag, TagResolver> tagResolverMap = new EnumMap<>(AdventureTag.class);

    private final Message.Format message;
    @Getter private final Permission.Message.Format permission;
    private final PlatformServerAdapter platformServerAdapter;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SkinService skinService;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public FormatModule(FileResolver fileResolver,
                        PlatformServerAdapter platformServerAdapter,
                        FPlayerService fPlayerService,
                        PlatformPlayerAdapter platformPlayerAdapter,
                        SkinService skinService,
                        PermissionChecker permissionChecker,
                        MessagePipeline messagePipeline,
                        MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getMessage().getFormat());

        this.message = fileResolver.getMessage().getFormat();
        this.permission = fileResolver.getPermission().getMessage().getFormat();
        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.skinService = skinService;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.messageProcessRegistry = messageProcessRegistry;
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

        addChildren(ColorModule.class);
        addChildren(EmojiModule.class);
        addChildren(FixationModule.class);
        addChildren(ImageModule.class);
        addChildren(MentionModule.class);
        addChildren(ModerationModule.class);
        addChildren(NameModule.class);
        addChildren(QuestionAnswerModule.class);
        addChildren(ScoreboardModule.class);
        addChildren(StyleModule.class);
        addChildren(SpoilerModule.class);
        addChildren(TranslateModule.class);
        addChildren(WorldModule.class);
        addChildren(LegacyMiniConvertor.class);

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void onDisable() {
        tagResolverMap.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.FORMATTING)) return;

        FEntity sender = messageContext.getSender();
        if (checkModulePredicates(sender)) return;

        getTagResolverMap()
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, messageContext.isFlag(MessageFlag.USER_MESSAGE)))
                .forEach(entry -> messageContext.addReplacementTag(entry.getValue()));

        FPlayer receiver = messageContext.getReceiver();

        if (sender instanceof FPlayer fPlayer && isCorrectTag(AdventureTag.PING, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PING, (argumentQueue, context) -> {
                int ping = fPlayerService.getPing(fPlayer);

                String string = resolveLocalization(receiver).getTags().get(AdventureTag.PING)
                        .replace("<ping>", String.valueOf(ping));

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.TPS, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.TPS, (argumentQueue, context) -> {
                String string = resolveLocalization(receiver).getTags().get(AdventureTag.TPS)
                        .replace("<tps>", platformServerAdapter.getTPS());

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.ONLINE, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ONLINE, (argumentQueue, context) -> {
                String string = resolveLocalization(receiver).getTags().get(AdventureTag.ONLINE)
                        .replace("<online>", String.valueOf(platformServerAdapter.getOnlinePlayerCount()));

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.COORDS, sender)) {
            PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(sender);
            if (coordinates != null) {
                messageContext.addReplacementTag(MessagePipeline.ReplacementTag.COORDS, (argumentQueue, context) -> {
                    String string = resolveLocalization(receiver).getTags().get(AdventureTag.COORDS)
                            .replace("<x>", String.valueOf(coordinates.x()))
                            .replace("<y>", String.valueOf(coordinates.y()))
                            .replace("<z>", String.valueOf(coordinates.z()));

                    Component component = messagePipeline.builder(sender, receiver, string).build();

                    return Tag.selfClosingInserting(component);
                });
            }
        }

        if (isCorrectTag(AdventureTag.STATS, sender)) {
            PlatformPlayerAdapter.Statistics statistics = platformPlayerAdapter.getStatistics(sender);
            if (statistics != null) {
                messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STATS, (argumentQueue, context) -> {
                    String string = resolveLocalization(receiver).getTags().get(AdventureTag.STATS)
                            .replace("<hp>", String.valueOf(statistics.health()))
                            .replace("<armor>", String.valueOf(statistics.armor()))
                            .replace("<exp>", String.valueOf(statistics.level()))
                            .replace("<food>", String.valueOf(statistics.food()))
                            .replace("<attack>", String.valueOf(statistics.damage()));

                    Component component = messagePipeline.builder(sender, receiver, string).build();

                    return Tag.selfClosingInserting(component);
                });
            }
        }

        if (isCorrectTag(AdventureTag.SKIN, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SKIN, (argumentQueue, context) -> {
                String url = skinService.getBodyUrl(sender);
                String string = resolveLocalization(receiver).getTags().get(AdventureTag.SKIN)
                        .replace("<message>", url);

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.ITEM, sender)) {
            Object itemStackObject = platformPlayerAdapter.getItem(sender.getUuid());

            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ITEM, (argumentQueue, context) -> {
                String string = resolveLocalization(receiver).getTags().get(AdventureTag.ITEM);

                return Tag.selfClosingInserting(messagePipeline.builder(sender, receiver, string)
                        .build()
                        .replaceText(TextReplacementConfig.builder()
                                .match("<message>")
                                .replacement(platformServerAdapter.translateItemName(itemStackObject, messageContext.isFlag(MessageFlag.TRANSLATE_ITEM)))
                                .build()
                        )
                );
            });
        }

        if (messageContext.isFlag(MessageFlag.URL) && isCorrectTag(AdventureTag.URL, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.URL, (argumentQueue, context) -> {
                Tag.Argument urlArgument = argumentQueue.peek();
                if (urlArgument == null) return Tag.selfClosingInserting(Component.empty());

                String url = toASCII(urlArgument.value());
                String string = resolveLocalization(receiver).getTags().get(AdventureTag.URL)
                        .replace("<message>", url);

                Component component = messagePipeline.builder(sender, receiver, string)
                        .flag(MessageFlag.URL, false)
                        .build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) {
            String messageContent = replaceAll(sender, receiver, messageContext.getMessage());
            messageContext.setMessage(messageContent);
        }
    }

    private void putKyoriTag(AdventureTag type, TagResolver tagResolver) {
        Message.Format.Tag tag = message.getTags().get(type);
        if (tag == null) return;
        if (!tag.isEnable()) return;

        tagResolverMap.put(type, tagResolver);
    }

    private String toASCII(String stringUrl) {
        if (stringUrl == null || stringUrl.isBlank()) return "";

        try {
            return new URL(stringUrl).toURI().toASCIIString();
        } catch (MalformedURLException | URISyntaxException e) {
            return "";
        }
    }

    private String replaceAll(FEntity sender, FEntity fReceiver, String message) {
        if (checkModulePredicates(sender)) return message;

        if (isCorrectTag(AdventureTag.IMAGE, sender)) {
            Localization.Message.Format localization = resolveLocalization(fReceiver);
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.IMAGE),
                    this.message.getTags().get(AdventureTag.IMAGE).getTrigger(),
                    localization.getTags().get(AdventureTag.IMAGE).replace("<message>", "$1")
            );
        }

        if (isCorrectTag(AdventureTag.URL, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.URL),
                    this.message.getTags().get(AdventureTag.URL).getTrigger(),
                    "<url:'$1'>"
            );
        }

        if (isCorrectTag(AdventureTag.PING, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.PING).getTrigger(), "<ping>");
        }

        if (isCorrectTag(AdventureTag.TPS, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.TPS).getTrigger(), "<tps>");
        }

        if (isCorrectTag(AdventureTag.ONLINE, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.ONLINE).getTrigger(), "<online>");
        }

        if (isCorrectTag(AdventureTag.COORDS, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.COORDS).getTrigger(), "<coords>");
        }

        if (isCorrectTag(AdventureTag.STATS, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.STATS).getTrigger(), "<stats>");
        }

        if (isCorrectTag(AdventureTag.SKIN, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.SKIN).getTrigger(), "<skin>");
        }

        if (isCorrectTag(AdventureTag.ITEM, sender)) {
            message = message
                    .replace(this.message.getTags().get(AdventureTag.ITEM).getTrigger(), "<item>");
        }

        String regex = "(?<!\\\\)<trigger>(.*?)(?<!\\\\)<trigger>";

        if (isCorrectTag(AdventureTag.SPOILER, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.SPOILER),
                    regex.replace("<trigger>", this.message.getTags().get(AdventureTag.SPOILER).getTrigger()),
                    "<spoiler:'$1'>"
            );
        }

        if (isCorrectTag(AdventureTag.BOLD, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.BOLD),
                    regex.replace("<trigger>", this.message.getTags().get(AdventureTag.BOLD).getTrigger()),
                    "<bold>$1</bold>"
            );
        }

        if (isCorrectTag(AdventureTag.ITALIC, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.ITALIC),
                    regex.replace("<trigger>", this.message.getTags().get(AdventureTag.ITALIC).getTrigger()),
                    "<italic>$1</italic>"
            );
        }

        if (isCorrectTag(AdventureTag.UNDERLINE, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.UNDERLINE),
                    regex.replace("<trigger>", this.message.getTags().get(AdventureTag.UNDERLINE).getTrigger()),
                    "<underlined>$1</underlined>"
            );
        }

        if (isCorrectTag(AdventureTag.OBFUSCATED, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.OBFUSCATED),
                    regex.replace("<trigger>", this.message.getTags().get(AdventureTag.OBFUSCATED).getTrigger()),
                    "<obfuscated>$1</obfuscated>"
            );
        }

        if (isCorrectTag(AdventureTag.STRIKETHROUGH, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.STRIKETHROUGH),
                    regex.replace("<trigger>", this.message.getTags().get(AdventureTag.STRIKETHROUGH).getTrigger()),
                    "<strikethrough>$1</strikethrough>"
            );
        }

        return message;
    }

    private String replaceAll(FEntity sender, String message, Permission.PermissionEntry permission, String trigger, String format) {
        if (checkModulePredicates(sender)) return message;
        if (!permissionChecker.check(sender, permission)) return message;

        return message.replaceAll(trigger, format);
    }

    private boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!message.getTags().get(adventureTag).isEnable()) return false;
        if (!tagResolverMap.containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission.getTags().get(adventureTag));
    }

    private boolean isCorrectTag(AdventureTag adventureTag, FEntity sender) {
        if (!message.getTags().get(adventureTag).isEnable()) return false;

        return permissionChecker.check(sender, permission.getTags().get(adventureTag));
    }
}
