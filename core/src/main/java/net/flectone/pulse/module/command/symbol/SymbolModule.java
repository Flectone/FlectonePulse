package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Arrays;
import java.util.Collections;

@Singleton
public class SymbolModule extends AbstractModuleCommand<Localization.Command.Symbol> {

    private final Command.Symbol command;
    private final Permission.Command.Symbol permission;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public SymbolModule(FileResolver fileResolver,
                        CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getSymbol(), Command::getSymbol);

        this.command = fileResolver.getCommand().getSymbol();
        this.permission = fileResolver.getPermission().getCommand().getSymbol();
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptCategory = addPrompt(0, Localization.Command.Prompt::getCategory);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .required(promptCategory, commandParserProvider.singleMessageParser(), categorySuggestion())
                .required(promptMessage, commandParserProvider.messageParser(), symbolSuggestion())
                .permission(permission.getName())
        );

        addPredicate(this::checkCooldown);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> categorySuggestion() {
        return (context, input) -> command.getCategories()
                .keySet()
                .stream()
                .map(Suggestion::suggestion)
                .toList();
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> symbolSuggestion() {
        return (context, input) -> {
            String inputString = input.input();
            String[] words = inputString.split(" ");
            if (words.length < 2) return Collections.emptyList();

            String category = words[1];
            if (!command.getCategories().containsKey(category)) return Collections.emptyList();

            String rawInput = inputString.substring(words[0].length() + words[1].length() + 2);
            String[] symbols = command.getCategories().get(category).split(" ");

            return Arrays.stream(symbols)
                    .map(symbol -> Suggestion.suggestion(rawInput + symbol))
                    .toList();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String message = getArgument(commandContext, 1);

        builder(fPlayer)
                .destination(command.getDestination())
                .format(s -> s.getFormat().replace("<message>", message))
                .message(message)
                .sound(getSound())
                .sendBuilt();
    }
}
