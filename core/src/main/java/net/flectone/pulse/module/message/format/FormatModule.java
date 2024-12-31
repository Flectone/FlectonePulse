package net.flectone.pulse.module.message.format;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.format.color.ColorModule;
import net.flectone.pulse.module.message.format.emoji.EmojiModule;
import net.flectone.pulse.module.message.format.image.ImageModule;
import net.flectone.pulse.module.message.format.mention.MentionModule;
import net.flectone.pulse.module.message.format.moderation.ModerationModule;
import net.flectone.pulse.module.message.format.name.NameModule;
import net.flectone.pulse.module.message.format.spoiler.SpoilerModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.HashMap;
import java.util.Map;


public abstract class FormatModule extends AbstractModuleMessage<Localization.Message.Format> {

    @Getter
    private final Map<TagType, TagResolver> tagResolverMap = new HashMap<>();

    private final Message.Format message;
    @Getter
    private final Permission.Message.Format permission;

    private final ServerUtil serverUtil;
    private final FPlayerManager fPlayerManager;
    private final PermissionUtil permissionUtil;
    private final ItemUtil itemUtil;

    @Inject
    private ComponentUtil componentUtil;

    public FormatModule(FileManager fileManager,
                        ServerUtil serverUtil,
                        FPlayerManager fPlayerManager,
                        PermissionUtil permissionUtil,
                        ItemUtil itemUtil) {
        super(localization -> localization.getMessage().getFormat());

        this.serverUtil = serverUtil;
        this.fPlayerManager = fPlayerManager;
        this.permissionUtil = permissionUtil;
        this.itemUtil = itemUtil;

        message = fileManager.getMessage().getFormat();
        permission = fileManager.getPermission().getMessage().getFormat();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        permission.getTags().values().forEach(this::registerPermission);

        registerPermission(permission.getAll());

        tagResolverMap.put(TagType.HOVER, StandardTags.hoverEvent());
        tagResolverMap.put(TagType.CLICK, StandardTags.clickEvent());
        tagResolverMap.put(TagType.COLOR, StandardTags.color());
        tagResolverMap.put(TagType.KEYBIND, StandardTags.keybind());
        tagResolverMap.put(TagType.TRANSLATABLE, StandardTags.translatable());
        tagResolverMap.put(TagType.TRANSLATABLE_FALLBACK, StandardTags.translatableFallback());
        tagResolverMap.put(TagType.INSERTION, StandardTags.insertion());
        tagResolverMap.put(TagType.FONT, StandardTags.font());
        tagResolverMap.put(TagType.DECORATION, StandardTags.decorations());
        tagResolverMap.put(TagType.GRADIENT, StandardTags.gradient());
        tagResolverMap.put(TagType.RAINBOW, StandardTags.rainbow());
        tagResolverMap.put(TagType.RESET, StandardTags.reset());
        tagResolverMap.put(TagType.NEWLINE, StandardTags.newline());
        tagResolverMap.put(TagType.TRANSITION, StandardTags.transition());
        tagResolverMap.put(TagType.SELECTOR, StandardTags.selector());
        tagResolverMap.put(TagType.SCORE, StandardTags.score());
        tagResolverMap.put(TagType.NBT, StandardTags.nbt());

        addChildren(ColorModule.class);
        addChildren(EmojiModule.class);
        addChildren(ImageModule.class);
        addChildren(MentionModule.class);
        addChildren(ModerationModule.class);
        addChildren(NameModule.class);
        addChildren(SpoilerModule.class);
        addChildren(WorldModule.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public TagResolver tpsTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!permissionUtil.has(sender, permission.getTags().get(TagType.TPS))) return TagResolver.empty();

        return TagResolver.resolver("tps", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.TPS).replace("<tps>", String.valueOf(serverUtil.getTPS()));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver onlineTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!permissionUtil.has(sender, permission.getTags().get(TagType.ONLINE))) return TagResolver.empty();

        return TagResolver.resolver("online", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.ONLINE).replace("<online>", String.valueOf(serverUtil.getOnlineCount()));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver pingTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!permissionUtil.has(sender, permission.getTags().get(TagType.PING))) return TagResolver.empty();

        return TagResolver.resolver("ping", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.PING).replace("<ping>", String.valueOf(fPlayerManager.get(sender)));

            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public abstract TagResolver coordsTag(FEntity sender, FEntity fReceiver);
    public abstract TagResolver statsTag(FEntity sender, FEntity fReceiver);

    public TagResolver skinTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!permissionUtil.has(sender, permission.getTags().get(TagType.SKIN))) return TagResolver.empty();

        return TagResolver.resolver("skin", (argumentQueue, context) -> {

            String string = resolveLocalization(fReceiver).getTags().get(TagType.SKIN).replace("<message>", fPlayerManager.getBodyURL(sender));
            Component component = componentUtil.builder(sender, fReceiver, string).build();

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver itemTag(FEntity sender, FEntity fReceiver) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!permissionUtil.has(sender, permission.getTags().get(TagType.ITEM))) return TagResolver.empty();

        Object itemStackObject = fPlayerManager.getItem(sender.getUuid());

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
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!permissionUtil.has(sender, permission.getTags().get(TagType.URL))) return TagResolver.empty();

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

        Localization.Message.Format localization = resolveLocalization(fReceiver);

        message = replaceAll(sender, message,
                permission.getTags().get(TagType.IMAGE),
                this.message.getTags().get(TagType.IMAGE).getTrigger(),
                localization.getTags().get(TagType.IMAGE).replace("<message>", "$1")
        );

        message = replaceAll(sender, message,
                permission.getTags().get(TagType.URL),
                this.message.getTags().get(TagType.URL).getTrigger(),
                "<url:'$1'>"
        );

        message = message
                .replace(this.message.getTags().get(TagType.PING).getTrigger(), "<ping>")
                .replace(this.message.getTags().get(TagType.TPS).getTrigger(), "<tps>")
                .replace(this.message.getTags().get(TagType.ONLINE).getTrigger(), "<online>")
                .replace(this.message.getTags().get(TagType.COORDS).getTrigger(), "<coords>")
                .replace(this.message.getTags().get(TagType.STATS).getTrigger(), "<stats>")
                .replace(this.message.getTags().get(TagType.SKIN).getTrigger(), "<skin>")
                .replace(this.message.getTags().get(TagType.ITEM).getTrigger(), "<item>");

        String regex = "<trigger>(.*?)<trigger>";
        message = replaceAll(sender, message,
                permission.getTags().get(TagType.SPOILER),
                regex.replace("<trigger>", this.message.getTags().get(TagType.SPOILER).getTrigger()),
                "<spoiler:'$1'>"
        );
        message = replaceAll(sender, message,
                permission.getTags().get(TagType.BOLD),
                regex.replace("<trigger>", this.message.getTags().get(TagType.BOLD).getTrigger()),
                "<bold>$1</bold>"
        );
        message = replaceAll(sender, message,
                permission.getTags().get(TagType.ITALIC),
                regex.replace("<trigger>", this.message.getTags().get(TagType.ITALIC).getTrigger()),
                "<italic>$1</italic>"
        );
        message = replaceAll(sender, message,
                permission.getTags().get(TagType.UNDERLINE),
                regex.replace("<trigger>", this.message.getTags().get(TagType.UNDERLINE).getTrigger()),
                "<underlined>$1</underlined>"
        );
        message = replaceAll(sender, message,
                permission.getTags().get(TagType.OBFUSCATED),
                regex.replace("<trigger>", this.message.getTags().get(TagType.OBFUSCATED).getTrigger()),
                "<obfuscated>$1</obfuscated>"
        );
        message = replaceAll(sender, message,
                permission.getTags().get(TagType.STRIKETHROUGH),
                regex.replace("<trigger>", this.message.getTags().get(TagType.STRIKETHROUGH).getTrigger()),
                "<strikethrough>$1</strikethrough>"
        );
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
