package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Provider;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.online.OnlineModule;
import net.flectone.pulse.module.command.toponline.ToponlineModule;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// only for modern paper based servers
public class MiniPlaceholdersIntegration implements FIntegration, PulseListener {

    private final Pattern bracesPattern = Pattern.compile("\\{([^}]*)}");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final Provider<MuteModule> muteModuleProvider;
    private final Provider<ConditionModule> conditionModuleProvider;
    private final Provider<AfkModule> afkModuleProvider;
    private final Provider<OnlineModule> onlineModuleProvider;
    private final Provider<ToponlineModule> toponlineModuleProvider;
    private final MessagePipeline messagePipeline;

    @Getter private final FLogger fLogger;

    private Expansion expansion;

    public MiniPlaceholdersIntegration(FileFacade fileFacade,
                                       FPlayerService fPlayerService,
                                       PlatformPlayerAdapter platformPlayerAdapter,
                                       PlatformServerAdapter platformServerAdapter,
                                       Provider<MuteModule> muteModuleProvider,
                                       Provider<ConditionModule> conditionModuleProvider,
                                       Provider<AfkModule> afkModuleProvider,
                                       Provider<OnlineModule> onlineModuleProvider,
                                       Provider<ToponlineModule> toponlineModuleProvider,
                                       MessagePipeline messagePipeline,
                                       FLogger fLogger) {
        this.fileFacade = fileFacade;
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.muteModuleProvider = muteModuleProvider;
        this.conditionModuleProvider = conditionModuleProvider;
        this.afkModuleProvider = afkModuleProvider;
        this.onlineModuleProvider = onlineModuleProvider;
        this.toponlineModuleProvider = toponlineModuleProvider;
        this.messagePipeline = messagePipeline;
        this.fLogger = fLogger;
    }


    @Override
    public String getIntegrationName() {
        return "MiniPlaceholders";
    }

    @Override
    public void hook() {
        if (expansion == null) {
            expansion = createExpansion();
        }

        expansion.register();

        logHook();
    }

    @Override
    public void unhook() {
        if (expansion != null) {
            expansion.unregister();
        }

        logUnhook();
    }

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        Set<TagResolver> resolvers = new ObjectArraySet<>();
        resolvers.add(MiniPlaceholders.globalPlaceholders());

        MessageContext messageContext = event.context();
        FEntity fSender = messageContext.sender();
        FEntity fReceiver = messageContext.receiver();

        // switch parsing
        if (!messageContext.isFlag(MessageFlag.PLACEHOLDER_CONTEXT_SENDER)) {
            FEntity tempFPlayer = fSender;
            fSender = fReceiver;
            fReceiver = tempFPlayer;
        }

        Audience sender = getAudienceOrDefault(fSender.uuid(), null);
        Audience receiver = null;

        if (sender != null) {
            receiver = getAudienceOrDefault(fReceiver.uuid(), sender);

            resolvers.add(MiniPlaceholders.audiencePlaceholders());
            resolvers.add(MiniPlaceholders.relationalPlaceholders());
        }

        TagResolver[] resolversArray = resolvers.toArray(new TagResolver[0]);
        String message = replaceMiniPlaceholders(messageContext.message(), resolversArray, sender, receiver);

        return event.withContext(messageContext.withMessage(message));
    }

    private Tag fColorPlaceholder(FPlayer fPlayer, String argument, FColor.Type... types) {
        if (argument == null) return MessagePipeline.ReplacementTag.emptyTag();
        if (!StringUtils.isNumeric(argument)) return MessagePipeline.ReplacementTag.emptyTag();

        Int2ObjectArrayMap<String> colorsMap = new Int2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
        for (FColor.Type type : types) {
            colorsMap.putAll(fPlayer.getFColors(type));
        }

        int colorNumber = Integer.parseInt(argument);
        return Tag.preProcessParsed(StringUtils.defaultString(colorsMap.get(colorNumber)));
    }

    private Audience getAudienceOrDefault(UUID uuid, Audience defaultAudience) {
        Audience audience = Bukkit.getPlayer(uuid);
        return audience == null ? defaultAudience : audience;
    }

    private String replaceMiniPlaceholders(String text, TagResolver[] resolvers, Audience sender, Audience receiver) {
        Matcher matcher = bracesPattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String content = matcher.group(1);

            Component parsedMessage = sender == null || receiver == null
                    ? miniMessage.deserialize(content, resolvers)
                    : miniMessage.deserialize(content, new RelationalAudience<>(sender, receiver), resolvers);

            // fix colors problems for custom RP
            // https://github.com/BertTowne/InlineHeads
            matcher.appendReplacement(result, miniMessage.serialize(parsedMessage).replaceAll("</#[0-9a-fA-F]+>", ""));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public Expansion createExpansion() {
        return Expansion.builder(BuildConfig.PROJECT_NAME.toLowerCase())
                .version(BuildConfig.PROJECT_VERSION)
                .author(BuildConfig.PROJECT_AUTHOR)
                // ignore required type error
                .audiencePlaceholder(Player.class, "mute_suffix", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(muteModuleProvider.get().getMuteSuffix(fPlayer, fPlayer));
                })
                .audiencePlaceholder(Player.class, "afk_duration", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(String.valueOf(afkModuleProvider.get().getAfkDuration(fPlayer)));
                })
                .audiencePlaceholder(Player.class, "afk_duration_formatted", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(afkModuleProvider.get().getAfkDurationFormatted(fPlayer, fPlayer));
                })
                .audiencePlaceholder(Player.class, "toponline", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    ToponlineModule toponlineModule = toponlineModuleProvider.get();
                    Optional<FPlayer> fTarget = toponlineModule.getPlayerByPosition(queue.pop().value());
                    if (fTarget.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

                    String json = messagePipeline.buildJsonString(messagePipeline.createContext(fTarget.get(), fPlayer, "<display_name>"));
                    return Tag.selfClosingInserting(GsonComponentSerializer.gson().deserialize(json));
                })
                .audiencePlaceholder(Player.class, "online", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    OnlineModule onlineModule = onlineModuleProvider.get();
                    String timeValue = onlineModule.parseTimeValue(fPlayer, fPlayer, queue.pop().value());
                    if (StringUtils.isEmpty(timeValue)) return null;

                    return Tag.preProcessParsed(timeValue);
                })
                .audiencePlaceholder(Player.class, "condition", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(StringUtils.defaultString(conditionModuleProvider.get().getConditionValue(queue.pop().value(), fPlayer)));
                })
                .audiencePlaceholder(Player.class, "fcolor", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return fColorPlaceholder(fPlayer, queue.pop().value(), FColor.Type.SEE, FColor.Type.OUT);
                })
                .audiencePlaceholder(Player.class, "fcolor_out", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return fColorPlaceholder(fPlayer, queue.pop().value(), FColor.Type.OUT);
                })
                .audiencePlaceholder(Player.class, "fcolor_see", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return fColorPlaceholder(fPlayer, queue.pop().value(), FColor.Type.SEE);
                })
                .audiencePlaceholder(Player.class, "setting", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    String argument = queue.pop().value();
                    SettingText settingText = SettingText.fromString(argument);
                    if (settingText != null) {
                        String value = fPlayer.getSetting(settingText);
                        if (settingText == SettingText.CHAT_NAME && value == null) return Tag.preProcessParsed("default");

                        return Tag.preProcessParsed(StringUtils.defaultString(value));
                    }

                    return Tag.preProcessParsed(fPlayer.isSetting(argument.toUpperCase()) ? "yes" : "no");
                })
                .audiencePlaceholder(Player.class, "player", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(fPlayer.name());
                })
                .audiencePlaceholder(Player.class, "ip", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(StringUtils.defaultString(fPlayer.ip()));
                })
                .audiencePlaceholder(Player.class, "ping", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(String.valueOf(platformPlayerAdapter.getPing(fPlayer)));
                })
                .audiencePlaceholder(Player.class, "format", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    String message = queue.pop().value();
                    String json = messagePipeline.buildJsonString(messagePipeline.createContext(fPlayer, message));

                    return Tag.selfClosingInserting(GsonComponentSerializer.gson().deserialize(json));
                })
                .globalPlaceholder("online", (_, _) ->
                        Tag.preProcessParsed(String.valueOf(platformServerAdapter.getOnlinePlayerCount()))
                )
                .globalPlaceholder("tps", (_, _) ->
                        Tag.preProcessParsed(platformServerAdapter.getTPS())
                )
                .build();
    }

}
