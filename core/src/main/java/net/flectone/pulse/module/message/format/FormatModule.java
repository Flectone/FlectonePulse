package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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


public abstract class FormatModule extends AbstractModuleMessage<Localization.Message.Format> {

    @Getter private final Map<TagType, TagResolver> tagResolverMap = new HashMap<>();

    private final Message.Format message;
    @Getter private final Permission.Message.Format permission;

    @Inject private ServerUtil serverUtil;
    @Inject private FPlayerService fPlayerService;
    @Inject private PlatformPlayerAdapter platformPlayerAdapter;
    @Inject private SkinService skinService;
    @Inject private PermissionUtil permissionUtil;
    @Inject private ItemUtil itemUtil;
    @Inject private ComponentUtil componentUtil;

    public FormatModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getFormat());

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
        if (!isCorrectTag(TagType.TPS, sender)) return TagResolver.empty();

        return TagResolver.resolver("tps", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.TPS).replace("<tps>", String.valueOf(serverUtil.getTPS()));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver onlineTag(FEntity sender, FEntity fReceiver) {
        if (!isCorrectTag(TagType.ONLINE, sender)) return TagResolver.empty();

        return TagResolver.resolver("online", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.ONLINE).replace("<online>", String.valueOf(serverUtil.getOnlineCount()));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver pingTag(FEntity sender, FEntity fReceiver) {
        if (!(sender instanceof FPlayer fPlayer)) return TagResolver.empty();
        if (!isCorrectTag(TagType.PING, sender)) return TagResolver.empty();

        return TagResolver.resolver("ping", (argumentQueue, context) -> {

            int ping = fPlayerService.getPing(fPlayer);
            String string = resolveLocalization(fReceiver).getTags().get(TagType.PING).replace("<ping>", String.valueOf(ping));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public abstract TagResolver coordsTag(FEntity sender, FEntity fReceiver);
    public abstract TagResolver statsTag(FEntity sender, FEntity fReceiver);

    public TagResolver skinTag(FEntity sender, FEntity fReceiver) {
        if (!isCorrectTag(TagType.SKIN, sender)) return TagResolver.empty();

        return TagResolver.resolver("skin", (argumentQueue, context) -> {

            String url = skinService.getBodyUrl(sender);
            String string = resolveLocalization(fReceiver).getTags().get(TagType.SKIN).replace("<message>", url);
            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver itemTag(FEntity sender, FEntity fReceiver) {
        if (!isCorrectTag(TagType.ITEM, sender)) return TagResolver.empty();

        Object itemStackObject = platformPlayerAdapter.getItem(sender.getUuid());

        return TagResolver.resolver("item", (argumentQueue, context) -> {
            String string = resolveLocalization(fReceiver).getTags().get(TagType.ITEM);
            return Tag.selfClosingInserting(componentUtil.builder(sender, fReceiver, string)
                    .build()
                    .replaceText(TextReplacementConfig.builder()
                            .match("<message>")
                            .replacement(itemUtil.translatableComponent(itemStackObject))
                            .build()
                    )
            );
        });
    }

    public TagResolver urlTag(FEntity sender, FEntity fReceiver) {
        if (!isCorrectTag(TagType.URL, sender)) return TagResolver.empty();

        return TagResolver.resolver("url", (argumentQueue, context) -> {

            Tag.Argument urlArgument = argumentQueue.peek();
            if (urlArgument == null) return Tag.selfClosingInserting(Component.empty());

            String url = urlArgument.value();
            Component component = componentUtil.builder(sender, fReceiver, resolveLocalization(fReceiver).getTags().get(TagType.URL).replace("<message>", url))
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
        if (!permissionUtil.has(sender, permission)) return message;

        return message.replaceAll(trigger, format);
    }

    public boolean isCorrectTag(TagType tagType, FEntity sender, boolean needPermission) {
        if (checkModulePredicates(sender)) return false;
        if (!message.getTags().get(tagType).isEnable()) return false;
        if (!tagResolverMap.containsKey(tagType)) return false;

        return !needPermission || permissionUtil.has(sender, permission.getTags().get(tagType));
    }

    public boolean isCorrectTag(TagType tagType, FEntity sender) {
        if (checkModulePredicates(sender)) return false;
        if (!message.getTags().get(tagType).isEnable()) return false;

        return permissionUtil.has(sender, permission.getTags().get(tagType));
    }
}
