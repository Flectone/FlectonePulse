package net.flectone.pulse.platform.formatter;

/**
 * Cleans up urls that go into chat components, where a raw ampersand or a non-ASCII host
 * would otherwise be mangled.
 * @author TheFaser
 */
public interface UrlFormatter {

    /**
     * Escapes ampersands so they are not read as legacy color codes.
     *
     * @param url the url
     * @return the escaped url
     */
    String escapeAmpersand(String url);

    /**
     * Restores ampersands escaped by {@link #escapeAmpersand(String)}.
     *
     * @param url the escaped url
     * @return the original url
     */
    String unescapeAmpersand(String url);

    /**
     * Converts an international domain name to its ASCII form, so the client can open it.
     *
     * @param url the url
     * @return the ASCII url, or the input unchanged if it cannot be converted
     */
    String toASCII(String url);

}
