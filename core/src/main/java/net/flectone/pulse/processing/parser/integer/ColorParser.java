package net.flectone.pulse.processing.parser.integer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class ColorParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final Message.Format.FColor message;
    private final Permission.Command.Chatcolor chatcolorPermission;
    private final PermissionChecker permissionChecker;
    private final ColorConverter colorConverter;
    private final StringParser<FPlayer> stringParser;

    private final List<String> hexSymbols = List.of(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f"
    );

    @Inject
    public ColorParser(FileResolver fileResolver,
                       PermissionChecker permissionChecker,
                       ColorConverter colorConverter) {
        this.message = fileResolver.getMessage().getFormat().getFcolor();
        this.chatcolorPermission = fileResolver.getPermission().getCommand().getChatcolor();
        this.permissionChecker = permissionChecker;
        this.colorConverter = colorConverter;
        this.stringParser = new StringParser<>(StringParser.StringMode.SINGLE);
    }

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        String currentInput = input.input();
        currentInput = currentInput.substring(currentInput.indexOf(" ") + 1);

        String[] args = currentInput.split(" ");

        String current = args.length == 0 || currentInput.endsWith(" ") ? "" : args[args.length - 1];

        int maxColors = message.getDefaultColors().size();
        boolean hasOtherPermission = permissionChecker.check(context.sender(), chatcolorPermission.getOther());
        if (!hasOtherPermission && args.length >= maxColors ||
                hasOtherPermission && args.length >= maxColors + 1) {
            return Collections.emptyList();
        }

        List<String> suggestions = new ArrayList<>();

        // basic suggestions
        List<String> constants = List.of("clear", "<gradient:#", "<", "#", "&");
        for (String constant : constants) {
            if (current.isEmpty() || constant.startsWith(current)) {
                suggestions.add(constant);
            }
        }

        if (current.startsWith("#") && current.length() < 7) {
            for (String symbol : hexSymbols) {
                suggestions.add(current + symbol);
            }

        } else if (current.startsWith("&") && current.length() == 1) {
            suggestions.addAll(colorConverter.getLegacyColors());

        } else if (!current.isEmpty()) {
            for (String color : colorConverter.getNamedColors()) {
                if (color.startsWith(current)) {
                    suggestions.add(color);
                }
            }
        }

        return suggestions;
    }
}
