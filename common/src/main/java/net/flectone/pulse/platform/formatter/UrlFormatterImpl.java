package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UrlFormatterImpl implements UrlFormatter {

    private static final String SAFE_AMPERSAND = "__AND__";

    @Override
    public @NonNull String escapeAmpersand(@Nullable String url) {
        if (StringUtils.isEmpty(url)) return "";

        return Strings.CS.replace(url, "&", SAFE_AMPERSAND);
    }

    @Override
    public @NonNull String unescapeAmpersand(@Nullable String url) {
        if (StringUtils.isEmpty(url)) return "";

        return Strings.CS.replace(url, SAFE_AMPERSAND, "&");
    }

    @Override
    public @NonNull String toASCII(@Nullable String url) {
        if (StringUtils.isEmpty(url)) return "";

        try {
            URI uri = new URI(url);

            // check syntax
            uri.toURL();

            return uri.toASCIIString();
        } catch (URISyntaxException | IllegalArgumentException | MalformedURLException _) {
            return "";
        }
    }

    @Override
    public @NonNull String getDomainName(@Nullable String url)  {
        if (StringUtils.isEmpty(url)) return "";

        try {
            return new URI(url).getHost();
        } catch (URISyntaxException _) {
            // fallback
            String remainder = url;

            String[] schemeParts = remainder.split("://", 2);
            if (schemeParts.length == 2) {
                remainder = schemeParts[1];
            }

            String[] userInfoParts = remainder.split("@", 2);
            if (userInfoParts.length == 2) {
                remainder = userInfoParts[1];
            }

            String[] pathParts = remainder.split("/", 2);
            if (pathParts.length == 2) {
                remainder = pathParts[0];
            }

            String[] queryParts = remainder.split("\\?", 2);
            if (queryParts.length == 2) {
                remainder = queryParts[0];
            }

            String[] fragmentParts = remainder.split("#", 2);
            if (fragmentParts.length == 2) {
                remainder = fragmentParts[0];
            }

            String[] portParts = remainder.split(":", 2);
            if (portParts.length == 2) {
                remainder = portParts[0];
            }

            return remainder;
        }

    }

}
