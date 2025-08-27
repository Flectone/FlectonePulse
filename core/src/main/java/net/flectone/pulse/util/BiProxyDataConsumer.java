package net.flectone.pulse.util;

import java.io.IOException;

@FunctionalInterface
public interface BiProxyDataConsumer<T, U> {

    void accept(T t, U u) throws IOException;

}
