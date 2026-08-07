package net.flectone.pulse.processing.converter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Renders a remote image as colored text so it can be shown in chat.
 * @author TheFaser
 */
public interface ImagePixelConverter {

    /**
     * Drops the cached rendering of an image, or of every image when no link is given.
     *
     * @param link the image url, or null to clear the whole cache
     */
    void invalidate(@Nullable String link);

    /**
     * Returns the rendered image, downloading and rendering it only on the first request.
     *
     * @param link the image url, may be null
     * @return one string per row, or an empty list if the image could not be used
     */
    @NonNull List<String> convertOrGetCache(@Nullable String link);

    /**
     * Downloads and renders the image, bypassing the cache.
     *
     * @param link the image url, may be null
     * @return one string per row, or an empty list if the image could not be used
     */
    @NonNull List<String> convert(@Nullable String link);

}
