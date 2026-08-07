package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
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
public class SymbolModuleImpl implements SymbolModule {

    private final FileFacade fileFacade;
    private final MessageDispatcher messageDispatcher;
    private final CommandParserProvider commandParserProvider;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final SocialService socialService;

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
        return (_, _) -> config().categories()
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
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullCategory())
                            .build()
                    )
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
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullPage())
                            .build()
                    )
                    .build()
            );

            return;
        }

        List<String> finalSymbols = Arrays.stream(symbols)
                .skip((long) (page - 1) * perPage)
                .limit(perPage)
                .toList();

        StringBuilder stringBuilder = new StringBuilder();

        // header
        stringBuilder
                .append(StringUtils.replaceEach(
                        localization(fPlayer).header(),
                        new String[]{"<category>", "<count>"},
                        new String[]{category, String.valueOf(size)}
                ))
                .append("<br>");

        // line
        for (String symbol : finalSymbols) {
            stringBuilder.append(Strings.CS.replace(localization(fPlayer).lineElement(), "<symbol>", symbol));
        }

        // footer
        String commandLine = "/" + commandModuleController.getCommandName(this) + " " + category;
        stringBuilder
                .append("<br>")
                .append(StringUtils.replaceEach(
                        localization(fPlayer).footer(),
                        new String[]{"<command>", "<prev_page>", "<next_page>", "<current_page>", "<last_page>"},
                        new String[]{
                                commandLine,
                                String.valueOf(page - 1),
                                String.valueOf(page + 1),
                                String.valueOf(page),
                                String.valueOf(countPage)
                        }
                ));

        String message = stringBuilder.toString();

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .sound(soundOrThrow())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(message)
                        .build()
                )
                .build()
        );
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
    public Localization.Command.Symbol localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().symbol();
    }
}
