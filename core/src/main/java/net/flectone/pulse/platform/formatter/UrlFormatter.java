package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class UrlFormatter {

    @Inject
    public UrlFormatter() {
    }

    public String toASCII(String stringUrl) {
        if (stringUrl == null || stringUrl.isBlank()) return "";

        try {
            return new URI(stringUrl).toASCIIString();
        } catch (URISyntaxException e) {
            return "";
        }
    }

}
