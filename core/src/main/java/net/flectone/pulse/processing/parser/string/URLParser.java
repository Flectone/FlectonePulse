package net.flectone.pulse.processing.parser.string;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class URLParser {

    @NonNull
    public Optional<URL> parse(@Nullable String string) {
        if (string == null) return Optional.empty();

        try {
            return Optional.of(new URI(string).toURL());
        } catch (URISyntaxException | MalformedURLException _) {
            return Optional.empty();
        }
    }

}
