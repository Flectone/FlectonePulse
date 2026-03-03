package net.flectone.pulse.module.command.symbol;

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
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SymbolModule implements ModuleCommand<Localization.Command.Symbol> {

    private final FileFacade fileFacade;
    private final EventDispatcher eventDispatcher;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final SoundPlayer soundPlayer;
    private final CommandParserProvider commandParserProvider;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptCategory = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::category);
        String promptNumber = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::number);
        commandModuleController.registerCommand(this, manager -> manager
                .required(promptCategory, commandParserProvider.singleMessageParser(), categorySuggestion())
                .optional(promptNumber, commandParserProvider.integerParser())
                .permission(permission().name())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> categorySuggestion() {
        return (context, input) -> config().categories()
                .keySet()
                .stream()
                .map(Suggestion::suggestion)
                .toList();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String category = commandModuleController.getArgument(this, commandContext, 0);
        if (!config().categories().containsKey(category)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Symbol>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Symbol::nullCategory)
                    .build()
            );

            return;
        }

        String[] symbols = config().categories().get(category).split(" ");

        int size = symbols.length;

        String promptNumber = commandModuleController.getPrompt(this, 1);
        Optional<Integer> optionalNumber = commandContext.optional(promptNumber);
        int page = optionalNumber.orElse(1);

        int perPage = config().perPage();

        int countPage = (int) Math.ceil((double) size / perPage);
        if (page > countPage || page < 1) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Symbol>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Symbol::nullPage)
                    .build()
            );

            return;
        }

        List<String> finalSymbols = Arrays.stream(symbols)
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        String header = StringUtils.replaceEach(
                localization(fPlayer).header(),
                new String[]{"<category>", "<count>"},
                new String[]{category, String.valueOf(size)}
        );

        MessageContext headerContext = messagePipeline.createContext(fPlayer, header);
        Component component = messagePipeline.build(headerContext).append(Component.newline());

        StringBuilder symbolLine = new StringBuilder();
        for (String symbol : finalSymbols) {
            String line = Strings.CS.replace(localization(fPlayer).lineElement(), "<symbol>", symbol);
            symbolLine.append(line);
        }

        MessageContext lineContext = messagePipeline.createContext(fPlayer, symbolLine.toString());
        component = component.append(messagePipeline.build(lineContext)).append(Component.newline());

        String commandLine = "/" + commandModuleController.getCommandName(this) + " " + category;
        String footer = StringUtils.replaceEach(
                localization(fPlayer).footer(),
                new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                new String[]{
                        commandLine,
                        String.valueOf(page - 1),
                        String.valueOf(page + 1),
                        String.valueOf(page),
                        String.valueOf(countPage)
                }
        );

        MessageContext footerContext = messagePipeline.createContext(fPlayer, footer);
        component = component.append(messagePipeline.build(footerContext));

        eventDispatcher.dispatch(new MessageSendEvent(name(), fPlayer, component));

        soundPlayer.play(soundOrThrow(), fPlayer);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_SYMBOL;
    }

    @Override
    public Command.Symbol config() {
        return fileFacade.command().symbol();
    }

    @Override
    public Permission.Command.Symbol permission() {
        return fileFacade.permission().command().symbol();
    }

    @Override
    public Localization.Command.Symbol localization(FEntity sender) {
        return fileFacade.localization(sender).command().symbol();
    }
}
