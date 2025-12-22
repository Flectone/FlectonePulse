package net.flectone.pulse.module.command.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Arrays;
import java.util.Collections;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SymbolModule extends AbstractModuleCommand<Localization.Command.Symbol> {

    private final FileFacade fileFacade;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptCategory = addPrompt(0, Localization.Command.Prompt::category);
        String promptMessage = addPrompt(1, Localization.Command.Prompt::message);
        registerCommand(manager -> manager
                .required(promptCategory, commandParserProvider.singleMessageParser(), categorySuggestion())
                .required(promptMessage, commandParserProvider.messageParser(), symbolSuggestion())
                .permission(permission().name())
        );
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> categorySuggestion() {
        return (context, input) -> config().categories()
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
            if (!config().categories().containsKey(category)) return Collections.emptyList();

            String rawInput = inputString.substring(words[0].length() + words[1].length() + 2);
            String[] symbols = config().categories().get(category).split(" ");

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
                .format(Localization.Command.Symbol::format)
                .destination(config().destination())
                .message(message)
                .tagResolvers(fResolver -> new TagResolver[]{inputTag(message)})
                .sound(soundOrThrow())
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_SYMBOL;
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

    private TagResolver inputTag(String message) {
        return TagResolver.resolver("input", (argumentQueue, context) ->
                Tag.preProcessParsed(message));
    }
}
