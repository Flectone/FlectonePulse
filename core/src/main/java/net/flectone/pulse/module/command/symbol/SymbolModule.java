package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Arrays;
import java.util.Collections;

@Singleton
public class SymbolModule extends AbstractModuleCommand<Localization.Command.Symbol> {

    @Getter private final Command.Symbol command;
    @Getter private final Permission.Command.Symbol permission;

    private final CommandRegistry commandRegistry;

    @Inject
    public SymbolModule(FileResolver fileResolver,
                        CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getSymbol(), null);

        this.commandRegistry = commandRegistry;

        command = fileResolver.getCommand().getSymbol();
        permission = fileResolver.getPermission().getCommand().getSymbol();

        addPredicate(this::checkCooldown);
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptCategory = getPrompt().getCategory();
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .required(promptCategory, commandRegistry.singleMessageParser(), categorySuggestion())
                        .required(promptMessage, commandRegistry.messageParser(), symbolSuggestion())
                        .permission(permission.getName())
                        .handler(this)
        );
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

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        builder(fPlayer)
                .destination(command.getDestination())
                .format(s -> s.getFormat().replace("<message>", message))
                .message(message)
                .sound(getSound())
                .sendBuilt();
    }
}
