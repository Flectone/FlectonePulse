package net.flectone.pulse.module.message.format.replacement;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ReplacementModule extends AbstractModuleLocalization<Localization.Message.Format.Replacement> {

    private final Map<String, Pattern> triggerPatterns = new LinkedHashMap<>();
    private final Cache<String, String> messageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100000)
            .build();
    private final Cache<String, Component> imageCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    private final Message.Format.Replacement message;
    private final Permission.Message.Format.Replacement permission;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final PlatformServerAdapter platformServerAdapter;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final SkinService skinService;
    private final UrlFormatter urlFormatter;
    private final MiniMessage defaultMiniMessage;
    private final PermissionChecker permissionChecker;
    private final FLogger fLogger;

    @Inject
    public ReplacementModule(FileResolver fileResolver,
                             ListenerRegistry listenerRegistry,
                             MessagePipeline messagePipeline,
                             FPlayerService fPlayerService,
                             PlatformServerAdapter platformServerAdapter,
                             PlatformPlayerAdapter platformPlayerAdapter,
                             SkinService skinService,
                             UrlFormatter urlFormatter,
                             PermissionChecker permissionChecker,
                             FLogger fLogger) {
        super(localization -> localization.getMessage().getFormat().getReplacement());

        this.message = fileResolver.getMessage().getFormat().getReplacement();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getReplacement();
        this.listenerRegistry = listenerRegistry;
        this.messagePipeline = messagePipeline;
        this.fPlayerService = fPlayerService;
        this.platformServerAdapter = platformServerAdapter;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.skinService = skinService;
        this.urlFormatter = urlFormatter;
        this.permissionChecker = permissionChecker;
        this.defaultMiniMessage = MiniMessage.miniMessage();
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        permission.getValues().values().forEach(this::registerPermission);

        listenerRegistry.register(ReplacementPulseListener.class);

        message.getTriggers().forEach((name, regex) ->
                triggerPatterns.put(name, Pattern.compile(regex))
        );
    }

    @Override
    public void onDisable() {
        triggerPatterns.clear();
        messageCache.invalidateAll();
        imageCache.invalidateAll();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void format(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        if (StringUtils.isEmpty(contextMessage)) return;

        String formattedMessage;
        try {
            formattedMessage = messageCache.get(contextMessage, () -> processMessage(contextMessage));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            formattedMessage = processMessage(contextMessage);
        }

        messageContext.setMessage(formattedMessage);
    }

    public void addTags(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        FPlayer receiver = messageContext.getReceiver();
        boolean isTranslateItem = messageContext.isFlag(MessageFlag.TRANSLATE_ITEM);

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.REPLACEMENT, (argumentQueue, context) -> {
            Tag.Argument argument = argumentQueue.peek();
            if (argument == null) return Tag.selfClosingInserting(Component.empty());

            String name = argument.value();
            if (!permissionChecker.check(sender, permission.getValues().get(name))) return Tag.selfClosingInserting(Component.empty());

            String replacement = resolveLocalization(receiver).getValues().get(name);
            if (replacement == null) return Tag.selfClosingInserting(Component.empty());

            List<String> values = new ArrayList<>();
            while (argumentQueue.hasNext()) {
                Tag.Argument groupArg = argumentQueue.pop();
                values.add(StringEscapeUtils.unescapeJava(groupArg.value()));
            }

            return switch (name) {
                case "ping" -> pingTag(sender, receiver);
                case "tps" -> tpsTag(sender, receiver);
                case "online" -> onlineTag(sender, receiver);
                case "coords" -> coordsTag(sender, receiver);
                case "stats" -> statsTag(sender, receiver);
                case "skin" -> skinTag(sender, receiver);
                case "item" -> itemTag(sender, receiver, isTranslateItem);
                case "url" -> {
                    if (values.size() < 2) yield Tag.selfClosingInserting(Component.empty());

                    yield urlTag(sender, receiver, values.get(1));
                }
                case "image" -> {
                    if (values.size() < 2) yield Tag.selfClosingInserting(Component.empty());

                    yield imageTag(sender, receiver, values.get(1));
                }
                case "spoiler" -> {
                    if (values.size() < 2) yield Tag.selfClosingInserting(Component.empty());

                    yield spoilerTag(sender, receiver, values.get(1), messageContext.getFlags());
                }
                default -> {
                    String[] searchList = new String[values.size()];
                    String[] replacementList = new String[values.size()];

                    for (int i = 0; i < values.size(); i++) {
                        searchList[i] = "<message_" + i + ">";
                        replacementList[i] = values.get(i);
                    }

                    replacement = StringUtils.replaceEach(replacement, searchList, replacementList);

                    Component component = messagePipeline.builder(sender, receiver, replacement)
                            .flag(MessageFlag.REPLACEMENT, false)
                            .build();

                    yield Tag.selfClosingInserting(component);
                }
            };
        });

        // deprecated resolvers
        if (permissionChecker.check(sender, permission.getValues().get("ping"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.PING, (argumentQueue, context) ->
                    pingTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("tps"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.TPS, (argumentQueue, context) ->
                    tpsTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("online"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ONLINE, (argumentQueue, context) ->
                    onlineTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("coords"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.COORDS, (argumentQueue, context) ->
                    coordsTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("stats"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STATS, (argumentQueue, context) ->
                    statsTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("skin"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SKIN, (argumentQueue, context) ->
                    skinTag(sender, receiver)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("item"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.ITEM, (argumentQueue, context) ->
                    itemTag(sender, receiver, isTranslateItem)
            );
        }

        if (permissionChecker.check(sender, permission.getValues().get("url"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.URL, (argumentQueue, context) -> {
                Tag.Argument argument = argumentQueue.peek();
                if (argument == null) return Tag.selfClosingInserting(Component.empty());

                return urlTag(sender, receiver, argument.value());
            });
        }

        if (permissionChecker.check(sender, permission.getValues().get("image"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.IMAGE, (argumentQueue, context) -> {
                Tag.Argument argument = argumentQueue.peek();
                if (argument == null) return Tag.selfClosingInserting(Component.empty());

                return imageTag(sender, receiver, argument.value());
            });
        }

        if (permissionChecker.check(sender, permission.getValues().get("spoiler"))) {
            messageContext.addReplacementTag(MessagePipeline.ReplacementTag.SPOILER, (argumentQueue, context) -> {
                Tag.Argument argument = argumentQueue.peek();
                if (argument == null) return Tag.selfClosingInserting(Component.empty());

                return spoilerTag(sender, receiver, argument.value(), messageContext.getFlags());
            });
        }
    }

    private String processMessage(String message) {
        List<MatchInfo> matches = new ArrayList<>();

        for (Map.Entry<String, Pattern> entry : triggerPatterns.entrySet()) {
            String name = entry.getKey();
            boolean isUrl = name.equals("url") || name.equals("image");
            Matcher matcher = entry.getValue().matcher(message);

            while (matcher.find()) {
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

    private Tag spoilerTag(FEntity sender, FPlayer receiver, String spoilerText, Map<MessageFlag, Boolean> flags) {
        // skip deprecated issue <spoiler:\>
        if (spoilerText.equals("\\")) return Tag.selfClosingInserting(Component.empty());

        // "\\" to have the original context like ||%stats%||
        Component spoilerComponent = messagePipeline.builder(sender, receiver, "\\" + spoilerText)
                .flags(new EnumMap<>(flags)) // extend message flags
                .flag(MessageFlag.TRANSLATE_ITEM, false) // we don't need to double format "|| %item% ||"
                .build();

        int length = PlainTextComponentSerializer.plainText().serialize(spoilerComponent).length();
        length = spoilerText.endsWith(" ") ? length : Math.max(1, length - 1);

        Localization.Message.Format.Replacement replacement = resolveLocalization(receiver);
        String format = StringUtils.replaceEach(
                replacement.getValues().getOrDefault("spoiler", ""),
                new String[]{"<message_1>", "<symbols>"},
                new String[]{spoilerText, StringUtils.repeat(replacement.getSpoilerSymbol(), length)}
        );

        Component component = messagePipeline.builder(sender, receiver, format)
                // don't set .flag(MessageFlag.REPLACEMENT, false) to format "|| %item% ||"
                .build();

        return Tag.selfClosingInserting(component);
    }

    private Tag pingTag(FEntity sender, FPlayer receiver) {
        if (!(sender instanceof FPlayer fPlayer)) return Tag.selfClosingInserting(Component.empty());

        int ping = fPlayerService.getPing(fPlayer);

        String format = Strings.CS.replace(
                resolveLocalization(receiver).getValues().getOrDefault("ping", ""),
                "<ping>",
                String.valueOf(ping)
        );

        Component component = messagePipeline.builder(fPlayer, receiver, format)
                .flag(MessageFlag.REPLACEMENT, false)
                .build();

        return Tag.selfClosingInserting(component);
    }

    private Tag tpsTag(FEntity sender, FPlayer receiver) {
        String format = Strings.CS.replace(
                resolveLocalization(receiver).getValues().getOrDefault("tps", ""),
                "<tps>",
                platformServerAdapter.getTPS()
        );

        Component component = messagePipeline.builder(sender, receiver, format)
                .flag(MessageFlag.REPLACEMENT, false)
                .build();

        return Tag.selfClosingInserting(component);
    }

    private Tag onlineTag(FEntity sender, FPlayer receiver) {
        String format = Strings.CS.replace(
                resolveLocalization(receiver).getValues().getOrDefault("online", ""),
                "<online>",
                String.valueOf(platformServerAdapter.getOnlinePlayerCount())
        );

        Component component = messagePipeline.builder(sender, receiver, format)
                .flag(MessageFlag.REPLACEMENT, false)
                .build();

        return Tag.selfClosingInserting(component);
    }

    private Tag coordsTag(FEntity sender, FPlayer receiver) {
        Component component = Component.empty();

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(sender);
        if (coordinates != null) {
            String format = StringUtils.replaceEach(
                    resolveLocalization(receiver).getValues().getOrDefault("coords", ""),
                    new String[]{"<x>", "<y>", "<z>"},
                    new String[]{
                            String.valueOf(coordinates.x()),
                            String.valueOf(coordinates.y()),
                            String.valueOf(coordinates.z())
                    }
            );

            component = messagePipeline.builder(sender, receiver, format)
                    .flag(MessageFlag.REPLACEMENT, false)
                    .build();
        }

        return Tag.selfClosingInserting(component);
    }

    private Tag statsTag(FEntity sender, FPlayer receiver) {
        Component component = Component.empty();

        PlatformPlayerAdapter.Statistics statistics = platformPlayerAdapter.getStatistics(sender);
        if (statistics != null) {
            String format = StringUtils.replaceEach(
                    resolveLocalization(receiver).getValues().getOrDefault("stats", ""),
                    new String[]{"<hp>", "<armor>", "<exp>", "<food>", "<attack>"},
                    new String[]{
                            String.valueOf(statistics.health()),
                            String.valueOf(statistics.armor()),
                            String.valueOf(statistics.level()),
                            String.valueOf(statistics.food()),
                            String.valueOf(statistics.damage())
                    }
            );

           component = messagePipeline.builder(sender, receiver, format)
                   .flag(MessageFlag.REPLACEMENT, false)
                   .build();
        }

        return Tag.selfClosingInserting(component);
    }

    private Tag skinTag(FEntity sender, FPlayer receiver) {
        String url = skinService.getBodyUrl(sender);
        String format = Strings.CS.replace(
                resolveLocalization(receiver).getValues().getOrDefault("skin", ""),
                "<message_1>",
                url
        );

        Component component = messagePipeline.builder(sender, receiver, format)
                .flag(MessageFlag.REPLACEMENT, false)
                .build();

        return Tag.selfClosingInserting(component);
    }

    private Tag itemTag(FEntity sender, FPlayer receiver, boolean isTranslateItem) {
        Object itemStackObject = platformPlayerAdapter.getItem(sender.getUuid());
        Component componentItem = platformServerAdapter.translateItemName(itemStackObject, isTranslateItem);

        String format = resolveLocalization(receiver).getValues().getOrDefault("item", "");
        Component componentFormat = messagePipeline.builder(sender, receiver, format)
                .flag(MessageFlag.REPLACEMENT, false)
                .tagResolvers(TagResolver.resolver("message_1", (argumentQueue, context) ->
                        Tag.selfClosingInserting(componentItem))
                )
                .build();

        return Tag.selfClosingInserting(componentFormat);
    }

    private Tag urlTag(FEntity sender, FPlayer receiver, String url) {
        url = urlFormatter.toASCII(url);
        if (url.isEmpty()) return Tag.selfClosingInserting(Component.empty());

        String string = Strings.CS.replace(
                resolveLocalization(receiver).getValues().getOrDefault("url", ""),
                "<message_1>",
                url
        );

        Component component = messagePipeline.builder(sender, receiver, string)
                .flag(MessageFlag.REPLACEMENT, false)
                .build();

        return Tag.selfClosingInserting(component);
    }

    private Tag imageTag(FEntity sender, FPlayer receiver, String url) {
        url = urlFormatter.toASCII(url);
        if (url.isEmpty()) return Tag.selfClosingInserting(Component.empty());

        Component componentPixels;
        try {
            componentPixels = createImageComponent(url);
        } catch (ExecutionException ignored) {
            return Tag.selfClosingInserting(Component.empty());
        }

        String string = Strings.CS.replace(
                resolveLocalization(receiver).getValues().getOrDefault("image", ""),
                "<message_1>",
                url
        );

        Component component = messagePipeline.builder(sender, receiver, string)
                .flag(MessageFlag.REPLACEMENT, false)
                .tagResolvers(TagResolver.resolver("pixels", (argumentQueue, context) ->
                        Tag.inserting(componentPixels))
                )
                .build();

        return Tag.selfClosingInserting(component);
    }

    public Component createImageComponent(String link) throws ExecutionException {
        return imageCache.get(link, () -> {
            FImage fImage = new FImage(link);

            Component component = Component.empty();

            try {
                List<String> pixels = fImage.convertImageUrl();
                if (pixels == null) return component;

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

            } catch (IOException ignored) {
                // return empty component
            }

            return component;
        });
    }
}
