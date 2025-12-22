package net.flectone.pulse.module.command.online;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.online.model.OnlineMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OnlineModule extends AbstractModuleCommand<Localization.Command.Online> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final TimeFormatter timeFormatter;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptType = addPrompt(0, Localization.Command.Prompt::type);
        String promptPlayer = addPrompt(1, Localization.Command.Prompt::player);
        registerCommand(manager -> manager
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
        );
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
        if (isModuleDisabledFor(fPlayer, true)) return;

        String type = getArgument(commandContext, 0);
        String target = getArgument(commandContext, 1);

        FPlayer targetFPlayer = fPlayerService.getFPlayer(target);
        if (targetFPlayer.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Online::nullPlayer)
                    .build()
            );

            return;
        }

        sendMessage(OnlineMetadata.<Localization.Command.Online>builder()
                .sender(targetFPlayer)
                .filterPlayer(fPlayer)
                .format(s -> switch (type) {
                    case "first" -> timeFormatter.format(
                            fPlayer,
                            System.currentTimeMillis() - platformPlayerAdapter.getFirstPlayed(targetFPlayer),
                            s.formatFirst()
                    );
                    case "last" -> platformPlayerAdapter.isOnline(targetFPlayer) && integrationModule.canSeeVanished(targetFPlayer, fPlayer)
                            ? s.formatCurrent()
                            : timeFormatter.format(fPlayer, System.currentTimeMillis() - platformPlayerAdapter.getLastPlayed(targetFPlayer), s.formatLast());
                    case "total" -> timeFormatter.format(fPlayer,
                            platformPlayerAdapter.getAllTimePlayed(targetFPlayer),
                            s.formatTotal()
                    );
                    default -> "";
                })
                .type(type)
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_ONLINE;
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
