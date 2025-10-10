package net.flectone.pulse.util.interceptor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.execution.scheduler.SchedulerRunnable;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.logging.FLogger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AsyncInterceptor implements MethodInterceptor {

    private final Provider<TaskScheduler> taskScheduler;
    private final Provider<PlatformServerAdapter> platformServerAdapter;
    private final FLogger fLogger;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!method.isAnnotationPresent(Async.class)) return invocation.proceed();
        if (method.getReturnType() != Void.TYPE) {
            throw new IllegalStateException("@Async can only be applied to void methods");
        }

        Async async = method.getAnnotation(Async.class);
        long delay = async.delay();
        boolean independent = async.independent();
        SchedulerRunnable task = () -> proceedSafely(invocation);

        if (delay > 0) {
            taskScheduler.get().runAsyncLater(task, delay);
        } else if (independent || platformServerAdapter.get().isPrimaryThread() || isRestrictedAsyncThread()) {
            taskScheduler.get().runAsync(task);
        } else {
            // already async
            task.run();
        }

        return null;
    }

    private boolean isRestrictedAsyncThread() {
        String threadName = Thread.currentThread().getName();

        return threadName.startsWith("Netty") || threadName.startsWith("Async Chat");
    }

    private void proceedSafely(MethodInvocation invocation) {
        try {
            invocation.proceed();
        } catch (Throwable e) {
            fLogger.warning(e);
        }
    }
}
