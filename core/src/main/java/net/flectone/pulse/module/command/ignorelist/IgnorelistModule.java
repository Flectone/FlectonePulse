package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnorelistModule extends AbstractModuleCommand<Localization.Command.Ignorelist> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final MessagePipeline messagePipeline;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;
    private final SoundPlayer soundPlayer;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptNumber = addPrompt(0, Localization.Command.Prompt::number);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
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
                    .format(Localization.Command.Ignorelist::empty)
                    .build()
            );

            return;
        }

        Localization.Command.Ignorelist localization = localization(fPlayer);

        int size = ignoreList.size();
        int perPage = config().perPage();
        int countPage = (int) Math.ceil((double) size / perPage);

        String prompt = getPrompt(0);
        Optional<Integer> optionalPage = commandContext.optional(prompt);
        Integer page = optionalPage.orElse(1);

        if (page > countPage || page < 1) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignorelist::nullPage)
                    .build()
            );

            return;
        }

        String commandLine = "/" + getCommandName();

        List<Ignore> finalIgnoreList = ignoreList.stream()
                 .skip((long) (page - 1) * perPage)
                 .limit(perPage)
                 .toList();

        String header = Strings.CS.replace(localization.header(), "<count>", String.valueOf(size));
        Component component = messagePipeline.builder(fPlayer, header)
                .build()
                .append(Component.newline());

        for (Ignore ignore : finalIgnoreList) {

            FPlayer fTarget = fPlayerService.getFPlayer(ignore.target());
            String line = StringUtils.replaceEach(
                    localization.line(),
                    new String[]{"<command>", "<date>"},
                    new String[]{"/ignore " + fTarget.getName(), timeFormatter.formatDate(ignore.date())}
            );

            component = component
                    .append(messagePipeline.builder(fTarget, fPlayer, line).build())
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localization.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{commandLine, String.valueOf(page - 1), String.valueOf(page + 1), String.valueOf(page), String.valueOf(countPage)}
        );

        component = component.append(messagePipeline.builder(fPlayer, footer).build());

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.COMMAND_IGNORELIST, fPlayer, component));

        soundPlayer.play(getModuleSound(), fPlayer);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_IGNORELIST;
    }

    @Override
    public Command.Ignorelist config() {
        return fileFacade.command().ignorelist();
    }

    @Override
    public Permission.Command.Ignorelist permission() {
        return fileFacade.permission().command().ignorelist();
    }

    @Override
    public Localization.Command.Ignorelist localization(FEntity sender) {
        return fileFacade.localization(sender).command().ignorelist();
    }
}
