package net.flectone.pulse.module.command.online;

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
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.online.listener.PulseOnlineListener;
import net.flectone.pulse.module.command.online.model.OnlineMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OnlineModule implements ModuleCommand<Localization.Command.Online> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final TimeFormatter timeFormatter;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptPlayer = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
        );

        listenerRegistry.register(PulseOnlineListener.class);
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (_, _) -> Arrays.stream(Type.values())
                .map(type -> Suggestion.suggestion(type.name().toLowerCase()))
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String type = commandModuleController.getArgument(this, commandContext, 0);
        String target = commandModuleController.getArgument(this, commandContext, 1);

        FPlayer targetFPlayer = fPlayerService.getFPlayer(target);
        PlayTime playTime = fPlayerService.getPlayTime(targetFPlayer);
        if (playTime == null) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Online>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Online::nullPlayer)
                    .build()
            );

            return;
        }

        messageDispatcher.dispatch(this, OnlineMetadata.<Localization.Command.Online>builder()
                .base(EventMetadata.<Localization.Command.Online>builder()
                        .sender(fPlayer)
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag(fResolver, targetFPlayer)
                        })
                        .format(localization -> switch (type.toUpperCase()) {
                            case "FIRST" -> timeFormatter.format(
                                    fPlayer,
                                    Type.FIRST.getTime(fPlayer, playTime),
                                    localization.formatFirst()
                            );
                            case "LAST" -> platformPlayerAdapter.isOnline(targetFPlayer) && integrationModule.canSeeVanished(targetFPlayer, fPlayer)
                                    ? localization.formatCurrent()
                                    : timeFormatter.format(fPlayer, Type.LAST.getTime(fPlayer, playTime), localization.formatLast());
                            case "TOTAL" -> Strings.CS.replace(
                                    timeFormatter.format(
                                            fPlayer,
                                            Type.TOTAL.getTime(fPlayer, playTime),
                                            localization.formatTotal()
                                    ),
                                    "<sessions>",
                                    String.valueOf(playTime.sessions())
                            );
                            default -> "";
                        })
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .type(type)
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_ONLINE;
    }

    @Override
    public Command.Online config() {
        return fileFacade.command().online();
    }

    @Override
    public Permission.Command.Online permission() {
        return fileFacade.permission().command().online();
    }

    @Override
    public Localization.Command.Online localization(FEntity sender) {
        return fileFacade.localization(sender).command().online();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (!(sender instanceof FPlayer fPlayer)) return messageContext;

        return messageContext.addTagResolver(TagResolver.resolver(MessagePipeline.ReplacementTag.ONLINE.getTagName(), (argumentQueue, _) -> {
            if (!argumentQueue.hasNext()) return MessagePipeline.ReplacementTag.emptyTag();

            String timeValue = parseTimeValue(fPlayer, messageContext.receiver(), argumentQueue.pop().value());
            if (StringUtils.isEmpty(timeValue)) return MessagePipeline.ReplacementTag.emptyTag();

            return Tag.preProcessParsed(timeValue);
        }));
    }

    @NonNull
    public String parseTimeValue(FPlayer fPlayer, FPlayer fReceiver, String time) {
        int lastIndex = time.lastIndexOf('_');
        if (lastIndex != -1) {
            time = time.substring(0, lastIndex);
        }

        Optional<OnlineModule.Type> optionalType = OnlineModule.Type.fromString(time);
        if (optionalType.isEmpty()) return "";

        OnlineModule.Type type = optionalType.get();
        return lastIndex != -1 ? getTimeFormatted(fPlayer, fReceiver, type) : String.valueOf(getTime(fPlayer, type));
    }

    public int getTime(FPlayer fPlayer, Type type) {
        if (moduleController.isDisabledFor(this, fPlayer)) return 0;

        PlayTime playTime = fPlayerService.getPlayTime(fPlayer);
        if (playTime == null) return 0;

        return (int) type.getTime(fPlayer, playTime) / 1000;
    }

    @NonNull
    public String getTimeFormatted(FPlayer fPlayer, FPlayer fReceiver, Type type) {
        int time = getTime(fPlayer, type);
        if (time == 0) return "";

        return timeFormatter.format(fReceiver, time * 1000L);
    }

    public enum Type {
        FIRST {
            @Override
            long getTime(FPlayer fPlayer, PlayTime playTime) {
                return System.currentTimeMillis() - playTime.first();
            }
        },
        LAST {
            @Override
            long getTime(FPlayer fPlayer, PlayTime playTime) {
                return System.currentTimeMillis() - (playTime.last() > 0 ? playTime.last() : playTime.last() * -1);
            }
        },
        TOTAL {
            @Override
            long getTime(FPlayer fPlayer, PlayTime playTime) {
                return playTime.total() + (fPlayer.isOnline() && playTime.last() > 0 ? System.currentTimeMillis() - playTime.last() : 0);
            }
        };

        abstract long getTime(FPlayer fPlayer, PlayTime playTime);

        public static Optional<Type> fromString(String string) {
            return Arrays.stream(Type.values())
                    .filter(type -> type.name().equalsIgnoreCase(string))
                    .findFirst();
        }

    }

}
