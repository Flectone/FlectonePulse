package net.flectone.pulse.module.command.flectonepulse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.FlectonePulse;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.util.logging.FLogger;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Singleton
public class FlectonepulseModule extends AbstractModuleCommand<Localization.Command.Flectonepulse> {

    private final Command.Flectonepulse command;
    private final Permission.Command.Flectonepulse permission;

    private final FlectonePulse flectonePulse;
    private final FileManager fileManager;
    private final CommandRegistry commandRegistry;
    private final TimeFormatter timeFormatter;
    private final FLogger fLogger;

    @Inject
    public FlectonepulseModule(FileManager fileManager,
                               CommandRegistry commandRegistry,
                               TimeFormatter timeFormatter,
                               FlectonePulse flectonePulse,
                               FLogger fLogger) {
        super(localization -> localization.getCommand().getFlectonepulse(), null);

        this.flectonePulse = flectonePulse;
        this.fileManager = fileManager;
        this.commandRegistry = commandRegistry;
        this.timeFormatter = timeFormatter;
        this.fLogger = fLogger;

        command = fileManager.getCommand().getFlectonepulse();
        permission = fileManager.getPermission().getCommand().getFlectonepulse();

        addPredicate(this::checkCooldown);
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptType = getPrompt().getType();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .literal("reload")
                        .optional(promptType, commandRegistry.playerParser(), SuggestionProvider.suggestingStrings("text"))
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptType = getPrompt().getType();
        Optional<String> optionalType = commandContext.optional(promptType);
        String type = optionalType.orElse("all");

        if (type.equals("text")) {
            fileManager.reload();

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Flectonepulse::getFormatTrueText)
                    .sound(getSound())
                    .sendBuilt();

            return;
        }

        try {
            Instant start = Instant.now();

            flectonePulse.reload();

            Instant end = Instant.now();

            String formattedTime = timeFormatter.format(fPlayer, Duration.between(start, end).toMillis());

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(flectonepulse -> flectonepulse.getFormatTrue().replace("<time>", formattedTime))
                    .sound(getSound())
                    .sendBuilt();

        } catch (Exception e) {
            fLogger.warning(e);

            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Flectonepulse::getFormatFalse)
                    .sendBuilt();
        }
    }
}
