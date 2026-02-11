package net.flectone.pulse.module.message.format.replacement;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.FImage;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.replacement.listener.ReplacementPulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.formatter.UrlFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReplacementModule extends AbstractModuleLocalization<Localization.Message.Format.Replacement> {

    private final Map<String, Pattern> triggerPatterns = new ConcurrentHashMap<>();
    private final MiniMessage defaultMiniMessage = MiniMessage.miniMessage();

    private final @Named("replacementMessage") Cache<String, String> messageCache;
    private final @Named("replacementImage") Cache<String, Component> imageCache;
    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SkinService skinService;
    private final UrlFormatter urlFormatter;
    private final PermissionChecker permissionChecker;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(ReplacementPulseListener.class);

        config().triggers().forEach((name, regex) ->
                triggerPatterns.put(name, Pattern.compile(regex))
        );
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().addAll(permission().values().values());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        triggerPatterns.clear();
        messageCache.invalidateAll();
        imageCache.invalidateAll();
    }

    @Override
    public MessageType messageType() {
        return MessageType.REPLACEMENT;
    }

    @Override
    public Message.Format.Replacement config() {
        return fileFacade.message().format().replacement();
    }

    @Override
    public Permission.Message.Format.Replacement permission() {
        return fileFacade.permission().message().format().replacement();
    }

    @Override
    public Localization.Message.Format.Replacement localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().replacement();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        String contextMessage = messageContext.message();
        if (StringUtils.isEmpty(contextMessage)) return messageContext;

        String formattedMessage;
        try {
            formattedMessage = messageCache.get(contextMessage, () -> processMessage(sender, contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = processMessage(sender, contextMessage);
        }

        return messageContext.withMessage(formattedMessage);
    }

    public MessageContext addTags(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;

        FPlayer receiver = messageContext.receiver();

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.REPLACEMENT, (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return MessagePipeline.ReplacementTag.emptyTag();

            String name = argument.value();
            if (!permissionChecker.check(sender, permission().values().get(name))) return MessagePipeline.ReplacementTag.emptyTag();

            String replacement = localization(receiver).values().get(name);
            if (replacement == null) return MessagePipeline.ReplacementTag.emptyTag();

            List<String> values = new ObjectArrayList<>();
            while (argumentQueue.hasNext()) {
                Tag.Argument groupArg = argumentQueue.pop();
                values.add(StringEscapeUtils.unescapeJava(groupArg.value()));
            }

            return switch (name) {
                case "ping" -> pingTag(messageContext);
                case "tps" -> tpsTag(messageContext);
                case "online" -> onlineTag(messageContext);
                case "coords" -> coordsTag(messageContext);
                case "stats" -> statsTag(messageContext);
                case "skin" -> skinTag(messageContext);
                case "item" -> itemTag(messageContext);
                case "url" -> {
                    if (values.size() < 2) yield MessagePipeline.ReplacementTag.emptyTag();

                    yield urlTag(messageContext, values.get(1));
                }
                case "image" -> {
                    if (values.size() < 2) yield MessagePipeline.ReplacementTag.emptyTag();

                    yield imageTag(messageContext, values.get(1));
                }
                case "spoiler" -> {
                    if (values.size() < 2) yield MessagePipeline.ReplacementTag.emptyTag();

                    yield spoilerTag(messageContext, values.get(1));
                }
                default -> {
                    String[] searchList = new String[values.size()];
                    String[] replacementList = new String[values.size()];

                    for (int i = 0; i < values.size(); i++) {
                        searchList[i] = "<message_" + i + ">";
                        replacementList[i] = values.get(i);
                    }

                    replacement = StringUtils.replaceEach(replacement, searchList, replacementList);

                    MessageContext componentContext = messagePipeline.createContext(sender, receiver, replacement)
                            .withFlags(messageContext.flags())
                            .addFlags(
                                    new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                                    new boolean[]{false, false}
                            );

                    Component component = messagePipeline.build(componentContext);

                    yield Tag.selfClosingInserting(component);
                }
            };
        });
    }

    private String processMessage(FEntity sender, String message) {
        List<MatchInfo> matches = new ObjectArrayList<>();

        for (Map.Entry<String, Pattern> entry : triggerPatterns.entrySet()) {
            String name = entry.getKey();
            boolean isUrl = name.equals("url") || name.equals("image");
            Matcher matcher = entry.getValue().matcher(message);

            while (matcher.find()) {
                // format only url/image for unknown senders
                if (sender.isUnknown() && !isUrl) continue;

                String replacement = buildReplacement(name, matcher, isUrl);
                matches.add(new MatchInfo(matcher.start(), matcher.end(), replacement));
            }
        }

        matches.sort(Comparator.comparingInt(MatchInfo::start));

        StringBuilder stringBuilder = new StringBuilder();
        int lastPos = 0;
        for (MatchInfo matchInfo : matches) {
            if (matchInfo.start() < lastPos) continue;

            stringBuilder.append(message, lastPos, matchInfo.start());
            stringBuilder.append(matchInfo.replacement());
            lastPos = matchInfo.end();
        }

        stringBuilder.append(message.substring(lastPos));

        return stringBuilder.toString();
    }

    private String buildReplacement(String name, Matcher matcher, boolean isUrl) {
        StringBuilder stringBuilder = new StringBuilder("<replacement:'").append(name);
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String groupText = matcher.group(i);
            stringBuilder
                    .append("':'")
                    .append(StringEscapeUtils.escapeJava(
                            isUrl ? urlFormatter.escapeAmpersand(groupText) : groupText
                    ));
        }

        return stringBuilder.append("'>").toString();
    }

    private record MatchInfo(int start, int end, String replacement) {}

    private Tag spoilerTag(MessageContext messageContext, String spoilerText) {
        // skip deprecated issue <spoiler:\>
        if (spoilerText.equals("\\")) return MessagePipeline.ReplacementTag.emptyTag();

        // "." to have the original context like ||%stats%||
        MessageContext spoilerContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), "." + spoilerText)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.TRANSLATE_ITEM},
                        new boolean[]{false, false} // we don't need to double format "|| %item% ||"
                );

        Component spoilerComponent = messagePipeline.build(spoilerContext);

        int length = PlainTextComponentSerializer.plainText().serialize(spoilerComponent).length();
        length = spoilerText.endsWith(" ") ? length : Math.max(1, length - 1);

        Localization.Message.Format.Replacement replacement = localization(messageContext.receiver());
        String format = StringUtils.replaceEach(
                replacement.values().getOrDefault("spoiler", ""),
                new String[]{"<message_1>", "<symbols>"},
                new String[]{spoilerText, StringUtils.repeat(replacement.spoilerSymbol(), length)}
        );

        MessageContext formatContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlag(MessageFlag.USER_MESSAGE, false); // don't set .withFlag(MessageFlag.REPLACEMENT, false) to format "|| %item% ||"

        Component component = messagePipeline.build(formatContext);

        return Tag.selfClosingInserting(component);
    }

    private Tag pingTag(MessageContext messageContext) {
        if (!(messageContext.sender() instanceof FPlayer fPlayer)) return MessagePipeline.ReplacementTag.emptyTag();

        int ping = platformPlayerAdapter.getPing(fPlayer);

        String format = Strings.CS.replace(
                localization(messageContext.receiver()).values().getOrDefault("ping", ""),
                "<value>",
                String.valueOf(ping)
        );

        MessageContext newContext = messagePipeline.createContext(fPlayer, messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                        new boolean[]{false, false}
                );

        Component component = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(component);
    }

    private Tag tpsTag(MessageContext messageContext) {
        String format = Strings.CS.replace(
                localization(messageContext.receiver()).values().getOrDefault("tps", ""),
                "<value>",
                platformServerAdapter.getTPS()
        );

        MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                        new boolean[]{false, false}
                );

        Component component = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(component);
    }

    private Tag onlineTag(MessageContext messageContext) {
        String format = Strings.CS.replace(
                localization(messageContext.receiver()).values().getOrDefault("online", ""),
                "<value>",
                String.valueOf(platformServerAdapter.getOnlinePlayerCount())
        );

        MessageContext context = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                        new boolean[]{false, false}
                );

        Component component = messagePipeline.build(context);

        return Tag.selfClosingInserting(component);
    }

    private Tag coordsTag(MessageContext messageContext) {
        Component component = Component.empty();

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(messageContext.sender());
        if (coordinates != null) {
            String format = StringUtils.replaceEach(
                    localization(messageContext.receiver()).values().getOrDefault("coords", ""),
                    new String[]{"<x>", "<y>", "<z>"},
                    new String[]{
                            String.valueOf(coordinates.x()),
                            String.valueOf(coordinates.y()),
                            String.valueOf(coordinates.z())
                    }
            );

            MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                    .withFlags(messageContext.flags())
                    .addFlags(
                            new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                            new boolean[]{false, false}
                    );

            component = messagePipeline.build(newContext);
        }

        return Tag.selfClosingInserting(component);
    }

    private Tag statsTag(MessageContext messageContext) {
        PlatformPlayerAdapter.Statistics statistics = platformPlayerAdapter.getStatistics(messageContext.sender());
        if (statistics == null) return MessagePipeline.ReplacementTag.emptyTag();

        String format = StringUtils.replaceEach(
                localization(messageContext.receiver()).values().getOrDefault("stats", ""),
                new String[]{"<hp>", "<armor>", "<exp>", "<food>", "<attack>"},
                new String[]{
                        String.valueOf(statistics.health()),
                        String.valueOf(statistics.armor()),
                        String.valueOf(statistics.level()),
                        String.valueOf(statistics.food()),
                        String.valueOf(statistics.damage())
                }
        );

        MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                        new boolean[]{false, false}
                );

        Component component = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(component);
    }

    private Tag skinTag(MessageContext messageContext) {
        String url = skinService.getBodyUrl(messageContext.sender());

        Component componentPixels;
        try {
            componentPixels = createImageComponent(url);
        } catch (ExecutionException ignored) {
            return MessagePipeline.ReplacementTag.emptyTag();
        }

        String format = Strings.CS.replace(
                localization(messageContext.receiver()).values().getOrDefault("skin", ""),
                "<message_1>",
                url
        );

        MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                        new boolean[]{false, false}
                )
                .addTagResolver(TagResolver.resolver("pixels", (argumentQueue, ctx) -> Tag.inserting(componentPixels)));

        Component component = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(component);
    }

    private Tag itemTag(MessageContext messageContext) {
        Object itemStackObject = platformPlayerAdapter.getItem(messageContext.sender().uuid());
        Component componentItem = platformServerAdapter.translateItemName(itemStackObject, messageContext.messageUUID(), messageContext.isFlag(MessageFlag.TRANSLATE_ITEM));

        String format = localization(messageContext.receiver()).values().getOrDefault("item", "");
        MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), format)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT},
                        new boolean[]{false, false}
                )
                .addTagResolver(TagResolver.resolver("message_1", (argumentQueue, ctx) -> Tag.selfClosingInserting(componentItem)));

        Component componentFormat = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(componentFormat);
    }

    private Tag urlTag(MessageContext messageContext, String url) {
        url = urlFormatter.toASCII(urlFormatter.unescapeAmpersand(url));
        if (url.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

        String string = Strings.CS.replace(
                localization(messageContext.receiver()).values().getOrDefault("url", ""),
                "<message_1>",
                url
        );

        MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), string)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT, MessageFlag.LEGACY_COLORS},
                        new boolean[]{false, false, false}
                );

        Component component = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(component);
    }

    private Tag imageTag(MessageContext messageContext, String url) {
        url = urlFormatter.toASCII(urlFormatter.unescapeAmpersand(url));
        if (url.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

        Component componentPixels;
        try {
            componentPixels = createImageComponent(url);
        } catch (ExecutionException ignored) {
            return MessagePipeline.ReplacementTag.emptyTag();
        }

        String string = Strings.CS.replace(
                localization(messageContext.receiver()).values().getOrDefault("image", ""),
                "<message_1>",
                url
        );

        MessageContext newContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), string)
                .withFlags(messageContext.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.REPLACEMENT, MessageFlag.LEGACY_COLORS},
                        new boolean[]{false, false, false}
                )
                .addTagResolver(TagResolver.resolver("pixels", (argumentQueue, ctx) -> Tag.inserting(componentPixels)));

        Component component = messagePipeline.build(newContext);

        return Tag.selfClosingInserting(component);
    }

    public Component createImageComponent(String link) throws ExecutionException {
        return imageCache.get(link, () -> {
            FImage fImage = new FImage(link);

            Component component = Component.empty();

            try {
                List<String> pixels = fImage.convertImageUrl();

                for (int i = 0; i < pixels.size(); i++) {
                    component = component
                            .append(Component.newline())
                            .append(defaultMiniMessage.deserialize(pixels.get(i)));

                    if (i == pixels.size() - 1) {
                        component = component
                                .append(Component.newline());
                    }
                }

                imageCache.put(link, component);

            } catch (Exception ignored) {
                // return empty component
            }

            return component;
        });
    }
}