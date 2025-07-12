package net.flectone.pulse.parser.integer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Singleton
public class DurationReasonParser implements ArgumentParser<FPlayer, Pair<Long, String>>, BlockingSuggestionProvider.Strings<FPlayer> {

    private static final Pattern DURATION_PATTERN = Pattern.compile("((\\d+|[1-9])[dhms])");
    private static final List<String> TIME_UNITS = List.of("d", "h", "m", "s");

    @Inject
    public DurationReasonParser() {
    }

    @Override
    public @NonNull ArgumentParseResult<Pair<Long, String>> parse(@NonNull CommandContext<FPlayer> commandContext, @NonNull CommandInput commandInput) {
        String rawDuration = "";

        int size = commandInput.remainingTokens();
        StringJoiner stringJoiner = new StringJoiner(" ");

        for(int i = 0; i < size; ++i) {
            String word = commandInput.readStringSkipWhitespace(false);

            if (i == 0) {
                rawDuration = word;
                continue;
            }

            stringJoiner.add(word);
        }

        if (rawDuration.isEmpty()) {
            return ArgumentParseResult.success(new Pair<>(-1L, null));
        }

        String otherInput = stringJoiner.toString();
        Matcher matcher = DURATION_PATTERN.matcher(rawDuration.toLowerCase());
        Duration duration = Duration.ZERO;

        int length = 0;
        while (matcher.find()) {
            String group = matcher.group();
            String timeUnit = group.substring(group.length() - 1);
            int timeValue = Integer.parseInt(group.substring(0, group.length() - 1));

            duration = switch (timeUnit) {
                case "d" -> duration.plusDays(timeValue);
                case "h" -> duration.plusHours(timeValue);
                case "m" -> duration.plusMinutes(timeValue);
                case "s" -> duration.plusSeconds(timeValue);
                default -> Duration.ZERO;
            };

            length += group.length();
        }

        if (!duration.isZero() && length == rawDuration.length()) {
            return ArgumentParseResult.success(new Pair<>(duration.toMillis(), otherInput));
        }

        return ArgumentParseResult.success(new Pair<>(-1L, rawDuration + " " + otherInput));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> commandContext, @NonNull CommandInput input) {
        if (input.isEmpty(true)) {
            return IntStream.range(1, 10)
                    .mapToObj(String::valueOf)
                    .toList();
        }

        if (Character.isLetter(input.lastRemainingCharacter())) {
            return Collections.emptyList();
        }

        String string = input.readString();
        return TIME_UNITS.stream()
                .filter(unit -> !string.contains(unit))
                .map(unit -> string + unit)
                .toList();
    }
}
