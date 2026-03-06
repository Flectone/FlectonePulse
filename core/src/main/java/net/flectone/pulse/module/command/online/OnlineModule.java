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
import net.flectone.pulse.model.util.PlayTime;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.online.model.OnlineMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.List;

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

    @Override
    public void onEnable() {
        String promptType = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::type);
        String promptPlayer = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> List.of(
                Suggestion.suggestion("first"),
                Suggestion.suggestion("last"),
                Suggestion.suggestion("total")
        );
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
                        .format(localization -> switch (type) {
                            case "first" -> timeFormatter.format(
                                    fPlayer,
                                    System.currentTimeMillis() - playTime.first(),
                                    localization.formatFirst()
                            );
                            case "last" -> platformPlayerAdapter.isOnline(targetFPlayer) && integrationModule.canSeeVanished(targetFPlayer, fPlayer)
                                    ? localization.formatCurrent()
                                    : timeFormatter.format(fPlayer, System.currentTimeMillis() - playTime.last(), localization.formatLast());
                            case "total" -> Strings.CS.replace(
                                    timeFormatter.format(
                                            fPlayer,
                                            playTime.total() + (targetFPlayer.isOnline() ? System.currentTimeMillis() - playTime.last() : 0),
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
}
