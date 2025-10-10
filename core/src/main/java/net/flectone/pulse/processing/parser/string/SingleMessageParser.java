package net.flectone.pulse.processing.parser.string;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SingleMessageParser implements ArgumentParser<FPlayer, String> {

    private final StringParser<FPlayer> stringParser = new StringParser<>(StringParser.StringMode.SINGLE);

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }
}
