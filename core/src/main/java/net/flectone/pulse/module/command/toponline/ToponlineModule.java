package net.flectone.pulse.module.command.toponline;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.SenderToReceiverMessageEvent;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Singleton
public class ToponlineModule extends AbstractModuleCommand<Localization.Command.Toponline> {

    private final Command.Toponline command;
    private final Permission.Command.Toponline permission;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final EventDispatcher eventDispatcher;
    private final TimeFormatter timeFormatter;
    private final FLogger fLogger;

    @Inject
    public ToponlineModule(FileResolver fileResolver,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           PlatformServerAdapter platformServerAdapter,
                           CommandParserProvider commandParserProvider,
                           MessagePipeline messagePipeline,
                           EventDispatcher eventDispatcher,
                           TimeFormatter timeFormatter,
                           FLogger fLogger) {
        super(localization -> localization.getCommand().getToponline(), Command::getToponline);

        this.command = fileResolver.getCommand().getToponline();
        this.permission = fileResolver.getPermission().getCommand().getToponline();
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.commandParserProvider = commandParserProvider;
        this.messagePipeline = messagePipeline;
        this.eventDispatcher = eventDispatcher;
        this.timeFormatter = timeFormatter;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            fLogger.warning("/toponline module is disabled! This is not supported on Fabric");
            return;
        }

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptNumber = addPrompt(0, Localization.Command.Prompt::getNumber);
        registerCommand(manager -> manager
               .permission(permission.getName())
               .optional(promptNumber, commandParserProvider.integerParser())
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptNumber = getPrompt(0);
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
                .replace("<command>", "/" + getCommandName())
                .replace("<prev_page>", String.valueOf(page-1))
                .replace("<next_page>", String.valueOf(page+1))
                .replace("<current_page>", String.valueOf(page))
                .replace("<last_page>", String.valueOf(countPage));

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        eventDispatcher.dispatch(new SenderToReceiverMessageEvent(fPlayer, component));

        playSound(fPlayer);
    }
}
