package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.URI;
import java.net.URISyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UrlFormatter {

    private static final String SAFE_AMPERSAND = "__AND__";

    public String escapeAmpersand(String url) {
        return Strings.CS.replace(url,"&", SAFE_AMPERSAND);
    }

    public String unescapeAmpersand(String url) {
        return Strings.CS.replace(url, SAFE_AMPERSAND, "&");
    }

    public String toASCII(String url) {
        if (StringUtils.isEmpty(url)) return "";

        try {
            return new URI(url).toASCIIString();
        } catch (URISyntaxException e) {
            return "";
        }
    }

}
