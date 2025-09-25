package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Arrays;
import java.util.Collections;

@Singleton
public class SymbolModule extends AbstractModuleCommand<Localization.Command.Symbol> {

    private final FileResolver fileResolver;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public SymbolModule(FileResolver fileResolver,
                        CommandParserProvider commandParserProvider) {
        super(MessageType.COMMAND_SYMBOL);

        this.fileResolver = fileResolver;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        String promptCategory = addPrompt(0, Localization.Command.Prompt::getCategory);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::getMessage);
        registerCommand(manager -> manager
                .required(promptCategory, commandParserProvider.singleMessageParser(), categorySuggestion())
                .required(promptMessage, commandParserProvider.messageParser(), symbolSuggestion())
                .permission(permission().getName())
        );
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> categorySuggestion() {
        return (context, input) -> config().getCategories()
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
            if (!config().getCategories().containsKey(category)) return Collections.emptyList();

            String rawInput = inputString.substring(words[0].length() + words[1].length() + 2);
            String[] symbols = config().getCategories().get(category).split(" ");

            return Arrays.stream(symbols)
                    .map(symbol -> Suggestion.suggestion(rawInput + symbol))
                    .toList();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String message = getArgument(commandContext, 1);

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(s -> Strings.CS.replace(s.getFormat(), "<message>", message))
                .destination(config().getDestination())
                .message(message)
                .sound(getModuleSound())
                .build()
        );
    }

    @Override
    public Command.Symbol config() {
        return fileResolver.getCommand().getSymbol();
    }

    @Override
    public Permission.Command.Symbol permission() {
        return fileResolver.getPermission().getCommand().getSymbol();
    }

    @Override
    public Localization.Command.Symbol localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getSymbol();
    }
}
