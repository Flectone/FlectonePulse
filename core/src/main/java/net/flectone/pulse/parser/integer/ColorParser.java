package net.flectone.pulse.parser.integer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.converter.ColorConverter;
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

    private final Message.Format.Color colorMessage;
    private final Permission.Command.Chatcolor chatcolorPermission;
    private final PermissionChecker permissionChecker;
    private final ColorConverter colorConverter;
    private final StringParser<FPlayer> stringParser;

    @Inject
    public ColorParser(FileResolver fileResolver,
                       PermissionChecker permissionChecker,
                       ColorConverter colorConverter) {
        this.colorMessage = fileResolver.getMessage().getFormat().getColor();
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

        int maxColors = colorMessage.getValues().size();
        boolean hasOtherPermission = permissionChecker.check(context.sender(), chatcolorPermission.getOther());
        if (!hasOtherPermission && args.length >= maxColors ||
                hasOtherPermission && args.length >= maxColors + 1) {
            return Collections.emptyList();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length < 2) {
            suggestions.add("clear");
        }

        if (current.isEmpty()) {
            suggestions.add("#");
            suggestions.add("&");
        } else if (current.startsWith("#") && current.length() <= 6 ||
                (current.startsWith("&") && current.length() == 1)) {
            colorConverter.getHexSymbolList()
                    .stream()
                    .map(s -> current + s)
                    .forEach(suggestions::add);
        }

        colorConverter.getMinecraftList()
                .stream()
                .filter(s -> s.startsWith(current))
                .forEach(suggestions::add);

        return suggestions;
    }
}
