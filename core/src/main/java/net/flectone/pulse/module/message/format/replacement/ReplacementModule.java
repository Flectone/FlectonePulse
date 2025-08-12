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
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ReplacementModule extends AbstractModuleLocalization<Localization.Message.Format.Replacement> {

    private final Map<String, Pattern> triggerPatterns = new LinkedHashMap<>();
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

    @Inject
    public ReplacementModule(FileResolver fileResolver,
                             ListenerRegistry listenerRegistry,
                             MessagePipeline messagePipeline,
                             FPlayerService fPlayerService,
                             PlatformServerAdapter platformServerAdapter,
                             PlatformPlayerAdapter platformPlayerAdapter,
                             SkinService skinService,
                             UrlFormatter urlFormatter) {
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
        this.defaultMiniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        permission.getValues().values().forEach(this::registerPermission);

        listenerRegistry.register(ReplacementPulseListener.class);

        message.getTriggers().forEach((name, regex) -> {
            triggerPatterns.put(name, Pattern.compile(regex));
        });
    }

    @Override
    public void onDisable() {
        triggerPatterns.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public String processMessage(String message) {
        if (StringUtils.isEmpty(message)) return message;

        String result = message;
        for (Map.Entry<String, Pattern> entry : triggerPatterns.entrySet()) {
            String name = entry.getKey();
            boolean isUrl = name.equals("url") || name.equals("image");

            Pattern pattern = entry.getValue();

            StringBuilder stringBuilder = new StringBuilder();
            Matcher matcher = pattern.matcher(result);

            while (matcher.find()) {
                StringBuilder finalReplacement = new StringBuilder("<replacement:'" + name);
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String groupText = matcher.group(i);
                    finalReplacement
                            .append("':'")
                            .append(StringEscapeUtils.escapeJava(
                                    !isUrl ? groupText : urlFormatter.escapeAmpersand(groupText))
                            );
                }

                finalReplacement.append("'>");
                matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(finalReplacement.toString()));
            }

            matcher.appendTail(stringBuilder);
            result = stringBuilder.toString();
        }

        return result;
    }

    public Tag spoilerTag(FEntity sender, FPlayer receiver, String spoilerText, Map<MessageFlag, Boolean> flags) {
        // skip deprecated issue <spoiler:\>
        if (spoilerText.equals("\\")) return Tag.selfClosingInserting(Component.empty());

        // "\\" to have the original context like ||%stats%||
        Component spoilerComponent = messagePipeline.builder(sender, receiver, "\\" + spoilerText)
                .flags(new EnumMap<>(flags)) // extend message flags
                .flag(MessageFlag.TRANSLATE_ITEM, false) // we don't need to double format "|| %item% ||"
                .build();

        int length = Math.max(PlainTextComponentSerializer.plainText().serialize(spoilerComponent).length() - 1, 1);

        Localization.Message.Format.Replacement replacement = resolveLocalization(receiver);
        String format = StringUtils.replaceEach(
                replacement.getValues().getOrDefault("spoiler", ""),
                new String[]{"<message_1>", "<symbols>"},
                new String[]{spoilerText, StringUtils.repeat(replacement.getHoverSymbol(), length)}
        );

        Component component = messagePipeline.builder(sender, receiver, format)
                // don't set .flag(MessageFlag.REPLACEMENT, false) to format "|| %item% ||"
                .build();

        return Tag.selfClosingInserting(component);
    }

    public Tag pingTag(FEntity sender, FPlayer receiver) {
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

    public Tag tpsTag(FEntity sender, FPlayer receiver) {
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

    public Tag onlineTag(FEntity sender, FPlayer receiver) {
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

    public Tag coordsTag(FEntity sender, FPlayer receiver) {
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

    public Tag statsTag(FEntity sender, FPlayer receiver) {
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

    public Tag skinTag(FEntity sender, FPlayer receiver) {
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

    public Tag itemTag(FEntity sender, FPlayer receiver, boolean isTranslateItem) {
        Object itemStackObject = platformPlayerAdapter.getItem(sender.getUuid());
        Component componentItem = platformServerAdapter.translateItemName(itemStackObject, isTranslateItem);

        String format = resolveLocalization(receiver).getValues().getOrDefault("item", "");
        Component componentFormat = messagePipeline.builder(sender, receiver, format)
                .flag(MessageFlag.REPLACEMENT, false)
                .tagResolvers(TagResolver.resolver("message_1", (argumentQueue, context) ->
                        Tag.inserting(componentItem))
                )
                .build();

        return Tag.selfClosingInserting(componentFormat);
    }

    public Tag urlTag(FEntity sender, FPlayer receiver, String url) {
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

    public Tag imageTag(FEntity sender, FPlayer receiver, String url) {
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
