package net.flectone.pulse.util.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.scheduler.SchedulerRunnable;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.logging.FLogger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

@Singleton
public class AsyncInterceptor implements MethodInterceptor {

    @Inject private TaskScheduler taskScheduler;
    @Inject private FLogger fLogger;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!method.isAnnotationPresent(Async.class)) return invocation.proceed();
        if (method.getReturnType() != Void.TYPE) {
            throw new IllegalStateException("@Async can only be applied to void methods");
        }

        long delay = method.getAnnotation(Async.class).delay();

        SchedulerRunnable task = () -> proceedSafely(invocation);

        if (delay > 0) {
            taskScheduler.runAsyncLater(task, delay);
        } else {
            taskScheduler.runAsync(task);
        }

        return null;
    }

    private void proceedSafely(MethodInvocation invocation) {
        try {
            invocation.proceed();
        } catch (Throwable e) {
            fLogger.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
