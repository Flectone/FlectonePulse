package net.flectone.pulse.resolver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Consumer;

// idea taken from GrimAC
// https://github.com/GrimAnticheat/Grim/blob/2.0/common/src/main/java/ac/grim/grimac/utils/reflection/ReflectionUtils.java

@Singleton
public class ReflectionResolverImpl implements ReflectionResolver {

    private final LibraryResolver libraryResolver;
    @Getter private final boolean paper;
    @Getter private final boolean folia;

    @Inject
    public ReflectionResolverImpl(LibraryResolver libraryResolver) {
        this.libraryResolver = libraryResolver;
        this.paper = hasClass("com.destroystokyo.paper.ParticleBuilder");
        this.folia = hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    // relocation hack
    @Override
    public boolean hasClass(String... classParts) {
        return hasClass(String.join("", classParts));
    }

    @Override
    public boolean hasClass(String className) {
        return resolveClass(className) != null;
    }

    @Override
    public void hasClassOrElse(String className, Consumer<LibraryResolver> libraryConsumer) {
        hasClassOrElse(className, true, libraryConsumer);
    }

    @Override
    public void hasClassOrElse(String className, boolean needChecking, Consumer<LibraryResolver> libraryConsumer) {
        if (!needChecking || !hasClass(className)) {
            libraryConsumer.accept(libraryResolver);
        }
    }

    @Override
    public @Nullable Class<?> resolveClass(String... classParts) {
        return resolveClass(String.join("", classParts));
    }

    @Override
    public @Nullable Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException _) {
            return null;
        }
    }

    @Override
    public boolean hasMethod(@NonNull Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return resolveMethod(clazz, methodName, parameterTypes) != null;
    }

    @Override
    public @Nullable Method resolveMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException _) {
            while (clazz != null) {
                try {
                    return clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException _) {
                    clazz = clazz.getSuperclass();
                }
            }
        }

        return null;
    }

    @Override
    public @Nullable MethodHandle unreflectMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes) {
        return unreflectMethod(resolveMethod(clazz, methodName, parameterTypes));
    }

    @Override
    public @Nullable MethodHandle unreflectMethod(@Nullable Method method) {
        if (method == null) return null;

        return unreflect(lookup -> lookup.unreflect(method));
    }

    @Override
    public @Nullable MethodHandle unreflect(HandleFunction<MethodHandles.Lookup, MethodHandle> handleFunction) {
        try {
            return handleFunction.apply(MethodHandles.lookup());
        } catch (IllegalAccessException _) {
            return null;
        }
    }

}
