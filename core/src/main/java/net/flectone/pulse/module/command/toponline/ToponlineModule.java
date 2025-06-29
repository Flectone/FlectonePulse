package net.flectone.pulse.module.command.toponline;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.util.DisableAction;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Singleton
public class ToponlineModule extends AbstractModuleCommand<Localization.Command.Toponline> {

    private final Command.Toponline command;
    private final Permission.Command.Toponline permission;

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final CommandRegistry commandRegistry;
    private final MessagePipeline messagePipeline;
    private final MessageSender messageSender;
    private final TimeFormatter timeFormatter;

    @Inject
    public ToponlineModule(FileManager fileManager,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           CommandRegistry commandRegistry,
                           MessagePipeline messagePipeline,
                           MessageSender messageSender,
                           TimeFormatter timeFormatter) {
        super(localization -> localization.getCommand().getToponline(), null);

        this.platformPlayerAdapter = platformPlayerAdapter;
        this.commandRegistry = commandRegistry;
        this.messagePipeline = messagePipeline;
        this.messageSender = messageSender;
        this.timeFormatter = timeFormatter;

        command = fileManager.getCommand().getToponline();
        permission = fileManager.getPermission().getCommand().getToponline();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
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
        String promptNumber = getPrompt().getNumber();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptNumber, commandRegistry.integerParser())
                        .handler(commandContext -> execute(commandContext.sender(), commandContext))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptNumber = getPrompt().getNumber();
        Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
        int page = optionalNumber.orElse(1);

        List<PlatformPlayerAdapter.PlayedTimePlayer> playedTimePlayers = platformPlayerAdapter.getPlayedTimePlayers()
                .stream()
                .sorted(Comparator.comparing(PlatformPlayerAdapter.PlayedTimePlayer::playedTime).reversed())
                .toList();

        int size = playedTimePlayers.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Toponline::getNullPage)
                    .sendBuilt();
            return;
        }

        List<PlatformPlayerAdapter.PlayedTimePlayer> finalPlayedTimePlayers = playedTimePlayers.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        Localization.Command.Toponline localization = resolveLocalization(fPlayer);

        String header = localization.getHeader().replace("<count>", String.valueOf(size));
        Component component = messagePipeline.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (PlatformPlayerAdapter.PlayedTimePlayer timePlayer : finalPlayedTimePlayers) {


            String line = localization.getLine()
                    .replace("<time_player>", timePlayer.name())
                    .replace("<time>", timeFormatter.format(fPlayer, timePlayer.playedTime()));

            component = component
                    .append(messagePipeline.builder(fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = localization.getFooter()
                .replace("<command>", "/" + getName(command))
                .replace("<prev_page>", String.valueOf(page-1))
                .replace("<next_page>", String.valueOf(page+1))
                .replace("<current_page>", String.valueOf(page))
                .replace("<last_page>", String.valueOf(countPage));

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        messageSender.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }
}
