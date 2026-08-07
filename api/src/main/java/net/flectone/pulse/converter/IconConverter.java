package net.flectone.pulse.converter;

import org.jspecify.annotations.Nullable;

import java.io.File;

/**
 * Turns a server icon image into the base64 form the status ping expects.
 * @author TheFaser
 */
public interface IconConverter {

    /**
     * Converts an icon file.
     *
     * @param icon the image on disk
     * @return the encoded icon, or null if the file is missing or not a usable image
     */
    @Nullable String convert(File icon);

}
