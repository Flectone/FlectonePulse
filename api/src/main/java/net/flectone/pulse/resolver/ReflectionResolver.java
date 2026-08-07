package net.flectone.pulse.resolver;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Reflection helpers used to work with server internals that differ between versions and forks,
 * and to detect which of them are present.
 * @author TheFaser
 */
public interface ReflectionResolver {

    /**
     * Whether the server is Paper or a Paper fork.
     *
     * @return true on Paper
     */
    boolean isPaper();

    /**
     * Whether the server is Folia, which schedules per region rather than on one main thread.
     *
     * @return true on Folia
     */
    boolean isFolia();

    /**
     * Whether a class is present, with its name given in pieces so the literal survives relocation.
     *
     * @param classParts the pieces of the class name, joined without a separator
     * @return true if the class is on the classpath
     */
    boolean hasClass(String... classParts);

    /**
     * Whether a class is present.
     *
     * @param className the fully qualified class name
     * @return true if the class is on the classpath
     */
    boolean hasClass(String className);

    /**
     * Downloads a library only when the platform does not already provide the given class.
     *
     * @param className the class that decides whether the download is needed
     * @param libraryConsumer registers the libraries to fetch
     */
    void hasClassOrElse(String className, Consumer<LibraryResolver> libraryConsumer);

    /**
     * Downloads a library, optionally skipping the class check and always fetching.
     *
     * @param className the class that decides whether the download is needed
     * @param needChecking whether to check at all; false always fetches
     * @param libraryConsumer registers the libraries to fetch
     */
    void hasClassOrElse(String className, boolean needChecking, Consumer<LibraryResolver> libraryConsumer);

    /**
     * Loads a class, with its name given in pieces so the literal survives relocation.
     *
     * @param classParts the pieces of the class name, joined without a separator
     * @return the class, or null if it is not on the classpath
     */
    @Nullable Class<?> resolveClass(String... classParts);

    /**
     * Loads a class.
     *
     * @param className the fully qualified class name
     * @return the class, or null if it is not on the classpath
     */
    @Nullable Class<?> resolveClass(String className);

    /**
     * Whether a method exists, searching the class and its superclasses.
     *
     * @param clazz the class to search
     * @param methodName the method name
     * @param parameterTypes the parameter types
     * @return true if the method exists
     */
    boolean hasMethod(@NonNull Class<?> clazz, String methodName, Class<?>... parameterTypes);

    /**
     * Finds a method, searching the class and its superclasses, including non-public ones.
     *
     * @param clazz the class to search
     * @param methodName the method name
     * @param parameterTypes the parameter types
     * @return the method, or null if it does not exist
     */
    @Nullable Method resolveMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes);

    /**
     * Finds a method and turns it into a handle, which is far cheaper to call repeatedly.
     *
     * @param clazz the class to search
     * @param methodName the method name
     * @param parameterTypes the parameter types
     * @return the handle, or null if the method is missing or inaccessible
     */
    @Nullable MethodHandle unreflectMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes);

    /**
     * Turns a method into a handle.
     *
     * @param method the method, may be null
     * @return the handle, or null if the method was null or inaccessible
     */
    @Nullable MethodHandle unreflectMethod(@Nullable Method method);

    /**
     * Runs a lookup that may fail on access, swallowing the failure.
     *
     * @param handleFunction performs the lookup
     * @return the handle, or null if access was denied
     */
    @Nullable MethodHandle unreflect(HandleFunction<MethodHandles.Lookup, MethodHandle> handleFunction);

    /**
     * A lookup that is allowed to fail with {@link IllegalAccessException}.
     *
     * @param <T> the lookup type
     * @param <R> the produced handle type
     */
    @FunctionalInterface
    interface HandleFunction<T, R> {

        /**
         * Performs the lookup.
         *
         * @param t the lookup
         * @return the handle
         * @throws IllegalAccessException if the member is not accessible
         */
        R apply(T t) throws IllegalAccessException;

    }
}
