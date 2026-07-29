package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.stream.listener.PulseStreamListener;
import net.flectone.pulse.module.command.stream.listener.StreamProxyMessageListener;
import net.flectone.pulse.module.command.stream.model.StreamMessageContext;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StreamModule implements ModuleCommand {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final CommandParserProvider commandParserProvider;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProxyRegistry proxyRegistry;

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptUrl = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::url);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptUrl, commandParserProvider.nativeMessageParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(StreamProxyMessageListener.class);
        }

        listenerRegistry.register(PulseStreamListener.class);
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (_, _) -> List.of(
                Suggestion.suggestion("start"),
                Suggestion.suggestion("end")
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String type = commandModuleController.getArgument(this, commandContext, 0);
        Boolean needStart = switch (type) {
            case "start" -> true;
            case "end" -> false;
            default -> null;
        };

        if (needStart == null) return;

        boolean isStream = localization(FPlayer.UNKNOWN).prefixTrue().equals(socialService.getSetting(fPlayer, SettingText.STREAM_PREFIX));

        if (isStream && needStart && !fPlayer.isUnknown()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).already())
                            .build()
                    )
                    .build()
            );

            return;
        }

        if (!isStream && !needStart) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).not())
                            .build()
                    )
                    .build()
            );

            return;
        }

        setStreamPrefix(fPlayer, needStart
                ? localization(FPlayer.UNKNOWN).prefixTrue()
                : StringUtils.isEmpty(localization(FPlayer.UNKNOWN).prefixFalse()) ? null : localization(FPlayer.UNKNOWN).prefixFalse()
        );

        if (needStart) {
            String promptUrl = commandModuleController.getPrompt(this, 1);
            Optional<String> optionalUrl = commandContext.optional(promptUrl);
            String rawString = optionalUrl.orElse("");

            String urls = Arrays.stream(rawString.split("\\s+"))
                    .filter(this::isUrl)
                    .map(url -> StringUtils.substringBefore(url, "?"))
                    .collect(Collectors.joining(" "));

            messageDispatcher.dispatch(this, EventMetadata.builder()
                    .range(config().range())
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .messageContext(fResolver -> StreamMessageContext.builder()
                            .base(MessageContext.builder()
                                    .sender(fPlayer)
                                    .receiver(fResolver)
                                    .message(localization(fResolver).formatStart())
                                    .tagResolver(urlTag(fPlayer, fResolver, urls))
                                    .build()
                            )
                            .turned(true)
                            .urls(urls)
                            .build()
                    )
                    .proxy(dataOutputStream -> dataOutputStream.writeString(urls))
                    .integration(() -> IntegrationMessageFormat.builder()
                            .format(string -> Strings.CS.replace(string, "<urls>", StringUtils.defaultString(urls)))
                            .messageNames(List.of(name().name() + "_START"))
                            .build()
                    )
                    .build()
            );
        } else {
            messageDispatcher.dispatch(this, EventMetadata.builder()
                    .destination(config().destination())
                    .messageContext(fResolver -> StreamMessageContext.builder()
                            .base(MessageContext.builder()
                                    .sender(fPlayer)
                                    .receiver(fResolver)
                                    .message(localization(fResolver).formatEnd())
                                    .build()
                            )
                            .turned(false)
                            .build()
                    )
                    .integration(() -> IntegrationMessageFormat.builder()
                            .messageNames(List.of(name().name() + "_END"))
                            .build()
                    )
                    .build()
            );
        }
    }

    public void setStreamPrefix(FPlayer fPlayer, String prefix) {
        if (Objects.equals(prefix, socialService.getSetting(fPlayer, SettingText.STREAM_PREFIX))) return;

        socialService.saveSetting(fPlayer, SettingText.STREAM_PREFIX, prefix);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_STREAM;
    }

    @Override
    public Command.Stream config() {
        return fileFacade.command().stream();
    }

    @Override
    public Permission.Command.Stream permission() {
        return fileFacade.permission().command().stream();
    }

    @Override
    public Localization.Command.Stream localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().stream();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (!(sender instanceof FPlayer fPlayer)) return messageContext;
        if (moduleController.isDisabledFor(this, fPlayer)) return messageContext;

        return messageContext.addTagResolver(messagePipeline.resolver(Set.of(MessagePipeline.ReplacementTag.STREAM.getTagName(), "stream_prefix"), (_, _) -> {
            String streamPrefix = socialService.getSetting(fPlayer, SettingText.STREAM_PREFIX);
            if (StringUtils.isEmpty(streamPrefix)) return MessagePipeline.ReplacementTag.emptyTag();
            if (!streamPrefix.contains("%")) return Tag.preProcessParsed(streamPrefix);

            return Tag.inserting(messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .receiver(messageContext.receiver())
                    .message(streamPrefix)
                    .flags(messageContext.flags())
                    .flag(MessageFlag.PLAYER_MESSAGE, false)
                    .build()
            ));
        }));
    }

    public TagResolver urlTag(FEntity sender, FPlayer receiver, String urls) {
        return messagePipeline.resolver("urls", (_, _) -> {
            String template = localization(receiver).urlTemplate();

            return Tag.selfClosingInserting(messagePipeline.build(MessageContext.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .flag(MessageFlag.LEGACY_COLOR_CONVERSION, false)
                    .message(String.join("<br>", Arrays.stream(urls.split(" "))
                            .map(url -> Strings.CS.replace(template, "<url>", url))
                            .toList()
                    ))
                    .build()
            ));
        });
    }

    private boolean isUrl(String string) {
        try {
            new URI(string).toURL();
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
