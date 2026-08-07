package net.flectone.pulse.util;

import java.io.IOException;

/**
 * Writes extra data into a proxy message. Separate from {@link java.util.function.Consumer}
 * because writing to a stream can fail.
 *
 * @param <T> the stream type
 * @author TheFaser
 */
@FunctionalInterface
public interface ProxyDataConsumer<T> {

    /**
     * Writes the data.
     *
     * @param t the stream to write to
     * @throws IOException if writing fails
     */
    void accept(T t) throws IOException;

}
