package net.flectone.pulse.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.AdventureTag;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Singleton
public class FormatPulseListener implements PulseListener {

    private final Message.Format message;
    private final Permission.Message.Format permission;
    private final FormatModule formatModule;
    private final PlatformServerAdapter platformServerAdapter;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SkinService skinService;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;

    @Inject
    public FormatPulseListener(FileResolver fileResolver,
                               FormatModule formatModule,
                               PlatformServerAdapter platformServerAdapter,
                               FPlayerService fPlayerService,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               SkinService skinService,
                               PermissionChecker permissionChecker,
                               MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getFormat();
        this.permission = fileResolver.getPermission().getMessage().getFormat();
        this.formatModule = formatModule;
        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.skinService = skinService;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
    }

    @Pulse
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        if (!messageContext.isFlag(MessageFlag.FORMATTING)) return;

        FEntity sender = messageContext.getSender();
        if (formatModule.checkModulePredicates(sender)) return;

        formatModule.getTagResolverMap()
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, messageContext.isFlag(MessageFlag.USER_MESSAGE)))
                .forEach(entry -> messageContext.addReplacementTag(entry.getValue()));

        FPlayer receiver = messageContext.getReceiver();

        if (sender instanceof FPlayer fPlayer && isCorrectTag(AdventureTag.PING, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PING, (argumentQueue, context) -> {
                int ping = fPlayerService.getPing(fPlayer);

                String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.PING)
                        .replace("<ping>", String.valueOf(ping));

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.TPS, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.TPS, (argumentQueue, context) -> {
                String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.TPS)
                        .replace("<tps>", platformServerAdapter.getTPS());

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.ONLINE, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ONLINE, (argumentQueue, context) -> {
                String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.ONLINE)
                        .replace("<online>", String.valueOf(platformServerAdapter.getOnlinePlayerCount()));

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.COORDS, sender)) {
            PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(sender);
            if (coordinates != null) {
                messageContext.addReplacementTag(MessagePipeline.ReplacementTag.COORDS, (argumentQueue, context) -> {
                    String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.COORDS)
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
                    String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.STATS)
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
                String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.SKIN)
                        .replace("<message>", url);

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.ITEM, sender)) {
            Object itemStackObject = platformPlayerAdapter.getItem(sender.getUuid());

            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ITEM, (argumentQueue, context) -> {
                String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.ITEM);

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
                String string = formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.URL)
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

    private String toASCII(String stringUrl) {
        if (stringUrl == null || stringUrl.isBlank()) return "";

        try {
            return new URL(stringUrl).toURI().toASCIIString();
        } catch (MalformedURLException | URISyntaxException e) {
            return "";
        }
    }

    private String replaceAll(FEntity sender, FEntity fReceiver, String message) {
        if (formatModule.checkModulePredicates(sender)) return message;

        if (isCorrectTag(AdventureTag.IMAGE, sender)) {
            Localization.Message.Format localization = formatModule.resolveLocalization(fReceiver);
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
        if (formatModule.checkModulePredicates(sender)) return message;
        if (!permissionChecker.check(sender, permission)) return message;

        return message.replaceAll(trigger, format);
    }

    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender, boolean needPermission) {
        if (!message.getTags().get(adventureTag).isEnable()) return false;
        if (!formatModule.getTagResolverMap().containsKey(adventureTag)) return false;

        return !needPermission || permissionChecker.check(sender, permission.getTags().get(adventureTag));
    }

    public boolean isCorrectTag(AdventureTag adventureTag, FEntity sender) {
        if (!message.getTags().get(adventureTag).isEnable()) return false;

        return permissionChecker.check(sender, permission.getTags().get(adventureTag));
    }
}
