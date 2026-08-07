package net.flectone.pulse.processing.parser.string;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UUIDParserImpl implements UUIDParser {

    @Nullable
    @Override
    public UUID parse(String string) {
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

}
