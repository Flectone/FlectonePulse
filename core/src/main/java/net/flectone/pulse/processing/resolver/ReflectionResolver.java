package net.flectone.pulse.processing.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Consumer;

// idea taken from GrimAC
// https://github.com/GrimAnticheat/Grim/blob/2.0/common/src/main/java/ac/grim/grimac/utils/reflection/ReflectionUtils.java

@Singleton
public class ReflectionResolver {

    private final LibraryResolver libraryResolver;
    @Getter private final boolean paper;

    @Inject
    public ReflectionResolver(LibraryResolver libraryResolver) {
        this.libraryResolver = libraryResolver;
        this.paper = hasClass("com.destroystokyo.paper.ParticleBuilder");
    }

    // relocation hack
    public boolean hasClass(String... classParts) {
        return hasClass(String.join("", classParts));
    }

    public boolean hasClass(String className) {
        return resolveClass(className) != null;
    }

    public void hasClassOrElse(String className, Consumer<LibraryResolver> libraryConsumer) {
        hasClassOrElse(className, true, libraryConsumer);
    }

    public void hasClassOrElse(String className, boolean needChecking, Consumer<LibraryResolver> libraryConsumer) {
        if (!needChecking || !hasClass(className)) {
            libraryConsumer.accept(libraryResolver);
        }
    }

    public @Nullable Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public boolean hasMethod(@NotNull Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return resolveMethod(clazz, methodName, parameterTypes) != null;
    }

    public @Nullable Method resolveMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            while (clazz != null) {
                try {
                    return clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException ignored) {
                    clazz = clazz.getSuperclass();
                }
            }
        }

        return null;
    }

    public @Nullable MethodHandle unreflectMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        return unreflectMethod(resolveMethod(clazz, methodName, parameterTypes));
    }

    public @Nullable MethodHandle unreflectMethod(@Nullable Method method) {
        if (method == null) return null;

        return unreflect(lookup -> lookup.unreflect(method));
    }

    public @Nullable MethodHandle unreflect(HandleFunction<MethodHandles.Lookup, MethodHandle> handleFunction) {
        try {
            return handleFunction.apply(MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @FunctionalInterface
    public interface HandleFunction<T, R> {

        R apply(T t) throws IllegalAccessException;

    }
}
