package net.flectone.pulse.parser.string;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class URLParserImpl implements URLParser {

    @NonNull
    @Override
    public Optional<URL> parse(@Nullable String string) {
        if (StringUtils.isEmpty(string)) return Optional.empty();

        try {
            return Optional.of(new URI(string).toURL());
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException _) {
            return Optional.empty();
        }
    }

}
