package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class UrlFormatter {

    private final String safeAmpersand = "__AND__";

    @Inject
    public UrlFormatter() {
    }

    public String escapeAmpersand(String url) {
        return Strings.CS.replace(url,"&", safeAmpersand);
    }

    public String unescapeAmpersand(String url) {
        return Strings.CS.replace(url, safeAmpersand, "&");
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
