package net.flectone.pulse.parser.string;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.model.FPlayer;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;

@Singleton
public class MessageParser implements ArgumentParser<FPlayer, String> {

    private final StringParser<FPlayer> stringParser;

    @Inject
    public MessageParser() {
        this.stringParser = new StringParser<>(StringParser.StringMode.GREEDY);
    }

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }
}
