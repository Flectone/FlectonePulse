package net.flectone.pulse.annotation;

import java.lang.annotation.*;

/**
 * Marks a method to be executed synchronously on the main server thread.
 * This ensures thread-safe access to Bukkit/Spigot API which requires main thread execution.
 *
 * @author TheFaser
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sync {

    /**
     * Delay in milliseconds before synchronous execution starts.
     * Zero means immediate execution on the next server tick.
     * Negative values are treated as zero.
     *
     * @return delay in milliseconds
     */
    long delay() default 0;

}