package net.flectone.pulse.module.command.online;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.TimeUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.List;

@Singleton
public class OnlineModule extends AbstractModuleCommand<Localization.Command.Online> {

    private final Command.Online command;
    private final Permission.Command.Online permission;

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandRegistry commandRegistry;
    private final IntegrationModule integrationModule;
    private final TimeUtil timeUtil;

    @Inject
    public OnlineModule(FileManager fileManager,
                        FPlayerService fPlayerService,
                        PlatformPlayerAdapter platformPlayerAdapter,
                        CommandRegistry commandRegistry,
                        IntegrationModule integrationModule,
                        TimeUtil timeUtil) {
        super(localization -> localization.getCommand().getOnline(), null);

        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.commandRegistry = commandRegistry;
        this.integrationModule = integrationModule;
        this.timeUtil = timeUtil;

        command = fileManager.getCommand().getOnline();
        permission = fileManager.getPermission().getCommand().getOnline();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptType = getPrompt().getType();
        String promptPlayer = getPrompt().getPlayer();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptType, commandRegistry.singleMessageParser(), typeSuggestion())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .handler(commandContext -> execute(commandContext.sender(), commandContext))
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
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
        String target = commandContext.get(promptPlayer);

        FPlayer targetFPlayer = fPlayerService.getFPlayer(target);
        if (targetFPlayer.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Online::getNullPlayer)
                    .sendBuilt();
            return;
        }

        String promptType = getPrompt().getType();
        String type = commandContext.get(promptType);

        builder(targetFPlayer)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> switch (type) {
                    case "first" -> timeUtil.format(
                            fPlayer,
                            System.currentTimeMillis() - platformPlayerAdapter.getFirstPlayed(targetFPlayer),
                            s.getFormatFirst()
                    );
                    case "last" -> targetFPlayer.isOnline() && !integrationModule.isVanished(targetFPlayer)
                            ? s.getFormatCurrent()
                            : timeUtil.format(fPlayer, System.currentTimeMillis() - platformPlayerAdapter.getLastPlayed(targetFPlayer), s.getFormatLast());
                    case "total" -> timeUtil.format(fPlayer,
                            platformPlayerAdapter.getAllTimePlayed(fPlayer),
                            s.getFormatTotal()
                    );
                    default -> "";
                })
                .sound(getSound())
                .sendBuilt();
    }
}
