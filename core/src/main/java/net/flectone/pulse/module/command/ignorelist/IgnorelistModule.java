package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
public class IgnorelistModule extends AbstractModuleCommand<Localization.Command.Ignorelist> {

    private final Command.Ignorelist command;
    private final Permission.Command.Ignorelist permission;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final MessagePipeline messagePipeline;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;
    private final SoundPlayer soundPlayer;

    @Inject
    public IgnorelistModule(FileResolver fileResolver,
                            FPlayerService fPlayerService,
                            EventDispatcher eventDispatcher,
                            MessagePipeline messagePipeline,
                            CommandParserProvider commandParserProvider,
                            TimeFormatter timeFormatter,
                            SoundPlayer soundPlayer) {
        super(localization -> localization.getCommand().getIgnorelist(), Command::getIgnorelist, MessageType.COMMAND_IGNORELIST);

        this.command = fileResolver.getCommand().getIgnorelist();
        this.permission = fileResolver.getPermission().getCommand().getIgnorelist();
        this.fPlayerService = fPlayerService;
        this.eventDispatcher = eventDispatcher;
        this.messagePipeline = messagePipeline;
        this.commandParserProvider = commandParserProvider;
        this.timeFormatter = timeFormatter;
        this.soundPlayer = soundPlayer;
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
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        List<Ignore> ignoreList = fPlayer.getIgnores();
        if (ignoreList.isEmpty()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignorelist::getEmpty)
                    .build()
            );

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
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignorelist::getNullPage)
                    .build()
            );

            return;
        }

        String commandLine = "/" + getCommandName();

        List<Ignore> finalIgnoreList = ignoreList.stream()
                 .skip((long) (page - 1) * perPage)
                 .limit(perPage)
                 .toList();

        String header = Strings.CS.replace(localization.getHeader(), "<count>", String.valueOf(size));
        Component component = messagePipeline.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Ignore ignore : finalIgnoreList) {

            FPlayer fTarget = fPlayerService.getFPlayer(ignore.target());
            String line = StringUtils.replaceEach(
                    localization.getLine(),
                    new String[]{"<command>", "<date>"},
                    new String[]{"/ignore " + fTarget.getName(), timeFormatter.formatDate(ignore.date())}
            );

            component = component
                    .append(messagePipeline.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localization.getFooter(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{commandLine, String.valueOf(page - 1), String.valueOf(page + 1), String.valueOf(page), String.valueOf(countPage)}
        );

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.COMMAND_IGNORELIST, fPlayer, component));

        soundPlayer.play(getModuleSound(), fPlayer);
    }
}
