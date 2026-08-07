package net.flectone.pulse.util;

import com.google.inject.*;

@Singleton
public final class LazyInstance<T> implements Provider<T> {

    private final Injector injector;
    private final TypeLiteral<T> type;

    private T value;

    @Inject
    public LazyInstance(Injector injector, TypeLiteral<T> type) {
        this.injector = injector;
        this.type = type;
    }

    @Override
    public T get() {
        if (value == null) {
            value = injector.getInstance(Key.get(type));
        }

        return value;
    }

}