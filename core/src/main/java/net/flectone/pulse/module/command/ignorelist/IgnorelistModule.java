package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.listener.MessagePulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ignore;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.provider.CommandParserProvider;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
public class IgnorelistModule extends AbstractModuleCommand<Localization.Command.Ignorelist> {

    private final Command.Ignorelist command;
    private final Permission.Command.Ignorelist permission;
    private final FPlayerService fPlayerService;
    private final MessagePulseListener messagePulseListener;
    private final MessagePipeline messagePipeline;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;

    @Inject
    public IgnorelistModule(FileResolver fileResolver,
                            FPlayerService fPlayerService,
                            MessagePulseListener messagePulseListener,
                            MessagePipeline messagePipeline,
                            CommandParserProvider commandParserProvider,
                            TimeFormatter timeFormatter) {
        super(localization -> localization.getCommand().getIgnorelist(), Command::getIgnorelist);

        this.command = fileResolver.getCommand().getIgnorelist();
        this.permission = fileResolver.getPermission().getCommand().getIgnorelist();
        this.fPlayerService = fPlayerService;
        this.messagePulseListener = messagePulseListener;
        this.messagePipeline = messagePipeline;
        this.commandParserProvider = commandParserProvider;
        this.timeFormatter = timeFormatter;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptNumber = addPrompt(0, Localization.Command.Prompt::getNumber);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
                .optional(promptNumber, commandParserProvider.integerParser())
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        List<Ignore> ignoreList = fPlayer.getIgnores();
        if (ignoreList.isEmpty()) {
            builder(fPlayer)
                    .format(Localization.Command.Ignorelist::getEmpty)
                    .sendBuilt();
            return;
        }

        Localization.Command.Ignorelist localization = resolveLocalization(fPlayer);

        int size = ignoreList.size();
        int perPage = command.getPerPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        String prompt = getPrompt(0);
        Optional<Integer> optionalPage = commandContext.optional(prompt);
        Integer page = optionalPage.orElse(1);

        if (page > countPage || page < 1) {
            builder(fPlayer)
                    .format(Localization.Command.Ignorelist::getNullPage)
                    .sendBuilt();
            return;
        }

        String commandLine = "/" + getCommandName();

        List<Ignore> finalIgnoreList = ignoreList.stream()
                 .skip((long) (page - 1) * perPage)
                 .limit(perPage)
                 .toList();
        String header = localization.getHeader().replace("<count>", String.valueOf(size));
        Component component = messagePipeline.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Ignore ignore : finalIgnoreList) {

            FPlayer fTarget = fPlayerService.getFPlayer(ignore.target());
            String line = localization.getLine()
                    .replace("<command>", "/ignore " + fTarget.getName())
                    .replace("<date>", timeFormatter.formatDate(ignore.date()));

            component = component
                    .append(messagePipeline.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = localization.getFooter()
                .replace("<command>", commandLine)
                .replace("<prev_page>", String.valueOf(page-1))
                .replace("<next_page>", String.valueOf(page+1))
                .replace("<current_page>", String.valueOf(page))
                .replace("<last_page>", String.valueOf(countPage));

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        messagePulseListener.sendMessage(fPlayer, component);

        playSound(fPlayer);
    }
}
