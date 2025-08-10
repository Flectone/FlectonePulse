package net.flectone.pulse.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.FormatModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.formatter.UrlFormatter;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.AdventureTag;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.regex.Pattern;

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
    private final UrlFormatter urlFormatter;

    @Inject
    public FormatPulseListener(FileResolver fileResolver,
                               FormatModule formatModule,
                               PlatformServerAdapter platformServerAdapter,
                               FPlayerService fPlayerService,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               SkinService skinService,
                               PermissionChecker permissionChecker,
                               MessagePipeline messagePipeline,
                               UrlFormatter urlFormatter) {
        this.message = fileResolver.getMessage().getFormat();
        this.permission = fileResolver.getPermission().getMessage().getFormat();
        this.formatModule = formatModule;
        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.skinService = skinService;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.urlFormatter = urlFormatter;
    }

    @Pulse
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        if (!messageContext.isFlag(MessageFlag.FORMATTING)) return;

        FEntity sender = messageContext.getSender();
        if (formatModule.isModuleDisabledFor(sender)) return;

        formatModule.getTagResolverMap()
                .entrySet()
                .stream()
                .filter(entry -> isCorrectTag(entry.getKey(), sender, messageContext.isFlag(MessageFlag.USER_MESSAGE)))
                .forEach(entry -> messageContext.addReplacementTag(entry.getValue()));

        FPlayer receiver = messageContext.getReceiver();

        if (sender instanceof FPlayer fPlayer && isCorrectTag(AdventureTag.PING, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PING, (argumentQueue, context) -> {
                int ping = fPlayerService.getPing(fPlayer);

                String string = Strings.CS.replace(
                        formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.PING),
                        "<ping>",
                        String.valueOf(ping)
                );

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.TPS, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.TPS, (argumentQueue, context) -> {
                String string = Strings.CS.replace(
                        formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.TPS),
                        "<tps>",
                        platformServerAdapter.getTPS()
                );

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.ONLINE, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ONLINE, (argumentQueue, context) -> {
                String string = Strings.CS.replace(
                        formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.ONLINE),
                        "<online>",
                        String.valueOf(platformServerAdapter.getOnlinePlayerCount())
                );

                Component component = messagePipeline.builder(sender, receiver, string).build();

                return Tag.selfClosingInserting(component);
            });
        }

        if (isCorrectTag(AdventureTag.COORDS, sender)) {
            PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(sender);
            if (coordinates != null) {
                messageContext.addReplacementTag(MessagePipeline.ReplacementTag.COORDS, (argumentQueue, context) -> {
                    String string = StringUtils.replaceEach(
                            formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.COORDS),
                            new String[]{"<x>", "<y>", "<z>"},
                            new String[]{
                                    String.valueOf(coordinates.x()),
                                    String.valueOf(coordinates.y()),
                                    String.valueOf(coordinates.z())
                            }
                    );

                    Component component = messagePipeline.builder(sender, receiver, string).build();

                    return Tag.selfClosingInserting(component);
                });
            }
        }

        if (isCorrectTag(AdventureTag.STATS, sender)) {
            PlatformPlayerAdapter.Statistics statistics = platformPlayerAdapter.getStatistics(sender);
            if (statistics != null) {
                messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STATS, (argumentQueue, context) -> {
                    String string = StringUtils.replaceEach(
                            formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.STATS),
                            new String[]{"<hp>", "<armor>", "<exp>", "<food>", "<attack>"},
                            new String[]{
                                    String.valueOf(statistics.health()),
                                    String.valueOf(statistics.armor()),
                                    String.valueOf(statistics.level()),
                                    String.valueOf(statistics.food()),
                                    String.valueOf(statistics.damage())
                            }
                    );

                    Component component = messagePipeline.builder(sender, receiver, string).build();

                    return Tag.selfClosingInserting(component);
                });
            }
        }

        if (isCorrectTag(AdventureTag.SKIN, sender)) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SKIN, (argumentQueue, context) -> {
                String url = skinService.getBodyUrl(sender);
                String string = Strings.CS.replace(
                        formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.SKIN),
                        "<message>",
                        url
                );

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

                String url = urlFormatter.toASCII(urlArgument.value());
                if (url.isEmpty()) return Tag.selfClosingInserting(Component.empty());

                String string = Strings.CS.replace(
                        formatModule.resolveLocalization(receiver).getTags().get(AdventureTag.URL),
                        "<message>",
                        url
                );

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

    private String replaceAll(FEntity sender, FEntity fReceiver, String message) {
        if (formatModule.isModuleDisabledFor(sender)) return message;

        String[] searchList = new String[9];
        String[] replacementList = new String[9];
        int index = 0;

        if (isCorrectTag(AdventureTag.PING, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.PING).getTrigger();
            replacementList[index] = "<ping>";
            index++;
        }

        if (isCorrectTag(AdventureTag.TPS, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.TPS).getTrigger();
            replacementList[index] = "<tps>";
            index++;
        }

        if (isCorrectTag(AdventureTag.ONLINE, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.ONLINE).getTrigger();
            replacementList[index] = "<online>";
            index++;
        }

        if (isCorrectTag(AdventureTag.COORDS, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.COORDS).getTrigger();
            replacementList[index] = "<coords>";
            index++;
        }

        if (isCorrectTag(AdventureTag.STATS, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.STATS).getTrigger();
            replacementList[index] = "<stats>";
            index++;
        }

        if (isCorrectTag(AdventureTag.SKIN, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.SKIN).getTrigger();
            replacementList[index] = "<skin>";
            index++;
        }

        if (isCorrectTag(AdventureTag.ITEM, sender)) {
            searchList[index] = this.message.getTags().get(AdventureTag.ITEM).getTrigger();
            replacementList[index] = "<item>";
            index++;
        }

        if (index > 0) {
            String[] trimmedSearchList = new String[index];
            String[] trimmedReplacementList = new String[index];
            System.arraycopy(searchList, 0, trimmedSearchList, 0, index);
            System.arraycopy(replacementList, 0, trimmedReplacementList, 0, index);
            message = StringUtils.replaceEach(message, trimmedSearchList, trimmedReplacementList);
        }

        if (isCorrectTag(AdventureTag.IMAGE, sender)) {
            Localization.Message.Format localization = formatModule.resolveLocalization(fReceiver);
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.IMAGE),
                    formatModule.getPatternsMap().get(AdventureTag.IMAGE),
                    Strings.CS.replace(localization.getTags().get(AdventureTag.IMAGE), "<message>", "$1")
            );
        }

        if (isCorrectTag(AdventureTag.URL, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.URL),
                    formatModule.getPatternsMap().get(AdventureTag.URL),
                    "<url:'$1'>"
            );
        }

        if (isCorrectTag(AdventureTag.SPOILER, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.SPOILER),
                    formatModule.getPatternsMap().get(AdventureTag.SPOILER),
                    "<spoiler:'$1'>"
            );
        }

        if (isCorrectTag(AdventureTag.BOLD, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.BOLD),
                    formatModule.getPatternsMap().get(AdventureTag.BOLD),
                    "<bold>$1</bold>"
            );
        }

        if (isCorrectTag(AdventureTag.ITALIC, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.ITALIC),
                    formatModule.getPatternsMap().get(AdventureTag.ITALIC),
                    "<italic>$1</italic>"
            );
        }

        if (isCorrectTag(AdventureTag.UNDERLINE, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.UNDERLINE),
                    formatModule.getPatternsMap().get(AdventureTag.UNDERLINE),
                    "<underlined>$1</underlined>"
            );
        }

        if (isCorrectTag(AdventureTag.OBFUSCATED, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.OBFUSCATED),
                    formatModule.getPatternsMap().get(AdventureTag.OBFUSCATED),
                    "<obfuscated>$1</obfuscated>"
            );
        }

        if (isCorrectTag(AdventureTag.STRIKETHROUGH, sender)) {
            message = replaceAll(sender, message,
                    permission.getTags().get(AdventureTag.STRIKETHROUGH),
                    formatModule.getPatternsMap().get(AdventureTag.STRIKETHROUGH),
                    "<strikethrough>$1</strikethrough>"
            );
        }

        return message;
    }

    private String replaceAll(FEntity sender, String message, Permission.PermissionEntry permission, Pattern trigger, String format) {
        if (formatModule.isModuleDisabledFor(sender)) return message;
        if (!permissionChecker.check(sender, permission)) return message;

        return RegExUtils.replaceAll((CharSequence) message, trigger, format);
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
