package net.flectone.pulse.util.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.execution.scheduler.SchedulerRunnable;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.logging.FLogger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

@Singleton
public class SyncInterceptor implements MethodInterceptor {

    @Inject private TaskScheduler taskScheduler;
    @Inject private PlatformServerAdapter platformServerAdapter;
    @Inject private FLogger fLogger;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (!method.isAnnotationPresent(Sync.class)) return invocation.proceed();
        if (method.getReturnType() != Void.TYPE) {
            throw new IllegalStateException("@Sync can only be applied to void methods");
        }

        long delay = method.getAnnotation(Sync.class).delay();

        SchedulerRunnable task = () -> proceedSafely(invocation);

        if (delay > 0) {
            taskScheduler.runSyncLater(task, delay);
        } else if (platformServerAdapter.isPrimaryThread()) {
            // already sync
            task.run();
        } else {
            taskScheduler.runSync(task);
        }

        return null;
    }

    private void proceedSafely(MethodInvocation invocation) {
        try {
            invocation.proceed();
        } catch (Throwable e) {
            fLogger.warning(e);
        }
    }
}
