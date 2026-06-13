package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import net.flectone.pulse.service.SocialService;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperMiniPlaceholdersIntegration implements FIntegration, PulseListener {

    private final Pattern bracesPattern = Pattern.compile("\\{([^}]*)}");

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
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

    private Audience getAudienceOrDefault(UUID uuid, Audience defaultAudience) {
        Audience audience = Bukkit.getPlayer(uuid);
        return audience == null ? defaultAudience : audience;
    }

    private String replaceMiniPlaceholders(String text, TagResolver[] resolvers, Audience sender, Audience receiver) {
        Matcher matcher = bracesPattern.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String content = matcher.group(1);

            MiniMessage miniMessage = MiniMessage.miniMessage();

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
                .audiencePlaceholder("mute_suffix", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(muteModuleProvider.get().getMuteSuffix(fPlayer, fPlayer));
                })
                .audiencePlaceholder("afk_duration", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(String.valueOf(afkModuleProvider.get().getAfkDuration(fPlayer)));
                })
                .audiencePlaceholder("afk_duration_formatted", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(afkModuleProvider.get().getAfkDurationFormatted(fPlayer, fPlayer));
                })
                .audiencePlaceholder("toponline", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    ToponlineModule toponlineModule = toponlineModuleProvider.get();
                    Optional<FPlayer> fTarget = toponlineModule.getPlayerByPosition(queue.pop().value());
                    if (fTarget.isEmpty()) return MessagePipeline.ReplacementTag.emptyTag();

                    String json = messagePipeline.buildJson(MessageContext.builder()
                            .sender(fTarget.get())
                            .receiver(fPlayer)
                            .message("<display_name>")
                            .build()
                    );
                    return Tag.selfClosingInserting(GsonComponentSerializer.gson().deserialize(json));
                })
                .audiencePlaceholder("online", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    OnlineModule onlineModule = onlineModuleProvider.get();
                    String timeValue = onlineModule.parseTimeValue(fPlayer, fPlayer, queue.pop().value());
                    if (StringUtils.isEmpty(timeValue)) return null;

                    return Tag.preProcessParsed(timeValue);
                })
                .audiencePlaceholder("condition", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return Tag.preProcessParsed(StringUtils.defaultString(conditionModuleProvider.get().getConditionValue(queue.pop().value(), fPlayer)));
                })
                .audiencePlaceholder("fcolor", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return fColorPlaceholder(fPlayer, queue.pop().value(), FColor.Type.SEE, FColor.Type.OUT);
                })
                .audiencePlaceholder("fcolor_out", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return fColorPlaceholder(fPlayer, queue.pop().value(), FColor.Type.OUT);
                })
                .audiencePlaceholder("fcolor_see", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);
                    return fColorPlaceholder(fPlayer, queue.pop().value(), FColor.Type.SEE);
                })
                .audiencePlaceholder("setting", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    String argument = queue.pop().value();
                    SettingText settingText = SettingText.fromString(argument);
                    if (settingText != null) {
                        String value = socialService.getSetting(fPlayer, settingText);
                        if (settingText == SettingText.CHAT_NAME && value == null) return Tag.preProcessParsed("default");

                        return Tag.preProcessParsed(StringUtils.defaultString(value));
                    }

                    return Tag.preProcessParsed(socialService.isSetting(fPlayer, argument.toUpperCase()) ? "yes" : "no");
                })
                .audiencePlaceholder("player", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(fPlayer.name());
                })
                .audiencePlaceholder("ip", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(StringUtils.defaultString(fPlayer.ip()));
                })
                .audiencePlaceholder("ping", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(String.valueOf(platformPlayerAdapter.getPing(fPlayer)));
                })
                .audiencePlaceholder("format", (player, queue, _) -> {
                    if (!queue.hasNext()) return Tag.selfClosingInserting(Component.empty());

                    String json = messagePipeline.buildJson(MessageContext.builder()
                            .sender(fPlayerService.getFPlayer(player))
                            .message(queue.pop().value())
                            .build()
                    );
                    return Tag.selfClosingInserting(GsonComponentSerializer.gson().deserialize(json));
                })
                .audiencePlaceholder("tps", (player, _, _) -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(player);

                    return Tag.preProcessParsed(platformServerAdapter.getTPS(fPlayer));
                })
                .globalPlaceholder("online", (_, _) ->
                        Tag.preProcessParsed(String.valueOf(platformServerAdapter.getOnlinePlayerCount()))
                )
                .build();
    }

    private Tag fColorPlaceholder(FPlayer fPlayer, String argument, FColor.Type... types) {
        if (argument == null) return MessagePipeline.ReplacementTag.emptyTag();
        if (!StringUtils.isNumeric(argument)) return MessagePipeline.ReplacementTag.emptyTag();

        Int2ObjectArrayMap<String> colorsMap = new Int2ObjectArrayMap<>(fileFacade.message().format().fcolor().defaultColors());
        for (FColor.Type type : types) {
            colorsMap.putAll(socialService.loadColors(fPlayer, type));
        }

        int colorNumber = Integer.parseInt(argument);
        return Tag.preProcessParsed(StringUtils.defaultString(colorsMap.get(colorNumber)));
    }

}
