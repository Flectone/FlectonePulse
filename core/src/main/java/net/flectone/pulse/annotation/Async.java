package net.flectone.pulse.annotation;

import java.lang.annotation.*;

/**
 * Marks a method for asynchronous execution.
 * Processed at runtime by FlectonePulse.
 *
 * @author TheFaser
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Async {

    /**
     * Delay in milliseconds before execution starts.
     * Zero means immediate execution.
     * Negative values are treated as zero.
     *
     * @return delay in milliseconds
     */
    long delay() default 0;

    /**
     * If true, the method always runs in a separate asynchronous thread.
     * If false, the method runs asynchronously only when called from a synchronous thread.
     *
     * @return true to force execution in a separate asynchronous thread
     */
    boolean independent() default false;

}
