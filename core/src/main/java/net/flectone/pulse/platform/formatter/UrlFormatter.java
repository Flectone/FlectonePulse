package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Singleton
public class UrlFormatter {

    @Inject
    public UrlFormatter() {
    }

    public String toASCII(String stringUrl) {
        if (stringUrl == null || stringUrl.isBlank()) return "";

        try {
            return new URL(stringUrl).toURI().toASCIIString();
        } catch (MalformedURLException | URISyntaxException e) {
            return "";
        }
    }

}
