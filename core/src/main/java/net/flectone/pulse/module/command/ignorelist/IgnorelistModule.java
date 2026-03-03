package net.flectone.pulse.module.command.ignorelist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnorelistModule implements ModuleCommand<Localization.Command.Ignorelist> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final CommandParserProvider commandParserProvider;
    private final TimeFormatter timeFormatter;
    private final SoundPlayer soundPlayer;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptNumber = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptNumber, commandParserProvider.integerParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        List<Ignore> ignoreList = fPlayer.ignores();
        if (ignoreList.isEmpty()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ignorelist>builder()
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

        String prompt = commandModuleController.getPrompt(this, 0);
        Optional<Integer> optionalPage = commandContext.optional(prompt);
        Integer page = optionalPage.orElse(1);

        if (page > countPage || page < 1) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ignorelist>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignorelist::nullPage)
                    .build()
            );

            return;
        }

        String commandLine = "/" + commandModuleController.getCommandName(this);

        List<Ignore> finalIgnoreList = ignoreList.stream()
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = Strings.CS.replace(localization.header(), "<count>", String.valueOf(size));
        MessageContext headerContext = messagePipeline.createContext(fPlayer, header);
        Component component = messagePipeline.build(headerContext).append(Component.newline());

        for (Ignore ignore : finalIgnoreList) {
            FPlayer fTarget = fPlayerService.getFPlayer(ignore.target());
            String line = StringUtils.replaceEach(
                    localization.line(),
                    new String[]{"<command>", "<date>"},
                    new String[]{"/ignore " + fTarget.name(), timeFormatter.formatDate(ignore.date())}
            );

            MessageContext lineContext = messagePipeline.createContext(fPlayer, line)
                    .addTagResolver(messagePipeline.targetTag(fPlayer, fTarget));

            component = component
                    .append(messagePipeline.build(lineContext))
                    .append(Component.newline());
        }

        String footer = StringUtils.replaceEach(
                localization.footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{commandLine, String.valueOf(page - 1), String.valueOf(page + 1), String.valueOf(page), String.valueOf(countPage)}
        );

        MessageContext footerContext = messagePipeline.createContext(fPlayer, footer);
        component = component.append(messagePipeline.build(footerContext));

        eventDispatcher.dispatch(new MessageSendEvent(ModuleName.COMMAND_IGNORELIST, fPlayer, component));

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_IGNORELIST;
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
