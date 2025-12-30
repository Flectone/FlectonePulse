package net.flectone.pulse.processing.parser.integer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.processing.converter.ColorConverter;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ColorParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private static final List<String> HEX_SYMBOLS = List.of(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f"
    );

    private final StringParser<FPlayer> stringParser = new StringParser<>(StringParser.StringMode.SINGLE);

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;

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

        int maxColors = fileFacade.message().format().fcolor().defaultColors().size();
        boolean hasOtherPermission = permissionChecker.check(context.sender(), fileFacade.permission().command().chatcolor().other());
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
            for (String symbol : HEX_SYMBOLS) {
                suggestions.add(current + symbol);
            }

        } else if (current.startsWith("&") && current.length() == 1) {
            suggestions.addAll(ColorConverter.LEGACY_COLORS);

        } else if (!current.isEmpty()) {
            for (String color : ColorConverter.NAMED_COLORS) {
                if (color.startsWith(current)) {
                    suggestions.add(color);
                }
            }
        }

        return suggestions;
    }
}
