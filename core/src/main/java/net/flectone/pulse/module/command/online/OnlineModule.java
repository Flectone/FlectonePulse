package net.flectone.pulse.module.command.online;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.module.command.online.model.OnlineMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.logging.FLogger;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.List;

@Singleton
public class OnlineModule extends AbstractModuleCommand<Localization.Command.Online> {

    private final Command.Online command;
    private final Permission.Command.Online permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final CommandParserProvider commandParserProvider;
    private final IntegrationModule integrationModule;
    private final TimeFormatter timeFormatter;
    private final FLogger fLogger;

    @Inject
    public OnlineModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        PlatformPlayerAdapter platformPlayerAdapter,
                        PlatformServerAdapter platformServerAdapter,
                        CommandParserProvider commandParserProvider,
                        IntegrationModule integrationModule,
                        TimeFormatter timeFormatter,
                        FLogger fLogger) {
        super(localization -> localization.getCommand().getOnline(), Command::getOnline, MessageType.COMMAND_ONLINE);

        this.command = fileResolver.getCommand().getOnline();
        this.permission = fileResolver.getPermission().getCommand().getOnline();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.commandParserProvider = commandParserProvider;
        this.integrationModule = integrationModule;
        this.timeFormatter = timeFormatter;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            fLogger.warning("/online module is disabled! This is not supported on Fabric");
            return;
        }

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptType = addPrompt(0, Localization.Command.Prompt::getType);
        String promptPlayer = addPrompt(1, Localization.Command.Prompt::getPlayer);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .required(promptPlayer, commandParserProvider.playerParser(command.isSuggestOfflinePlayers()))
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
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
        if (isModuleDisabledFor(fPlayer)) return;

        String type = getArgument(commandContext, 0);
        String target = getArgument(commandContext, 1);

        FPlayer targetFPlayer = fPlayerService.getFPlayer(target);
        if (targetFPlayer.isUnknown()) {
            sendMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Online::getNullPlayer)
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
                            s.getFormatFirst()
                    );
                    case "last" -> platformPlayerAdapter.isOnline(targetFPlayer) && integrationModule.canSeeVanished(targetFPlayer, fPlayer)
                            ? s.getFormatCurrent()
                            : timeFormatter.format(fPlayer, System.currentTimeMillis() - platformPlayerAdapter.getLastPlayed(targetFPlayer), s.getFormatLast());
                    case "total" -> timeFormatter.format(fPlayer,
                            platformPlayerAdapter.getAllTimePlayed(targetFPlayer),
                            s.getFormatTotal()
                    );
                    default -> "";
                })
                .type(type)
                .destination(command.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
