package net.flectone.pulse.util.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Singleton
public class AsyncInterceptor implements MethodInterceptor {

    @Inject private TaskScheduler taskScheduler;
    @Inject private FLogger fLogger;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        if (method.isAnnotationPresent(Async.class)) {
            CompletableFuture<Object> future = new CompletableFuture<>();

            taskScheduler.runAsync(() -> proceed(method, invocation, future));
            return future;
        }

        return invocation.proceed();
    }

    private void proceed(Method method, Invocation invocation, CompletableFuture<Object> future) {
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                invocation.proceed();
                future.complete(null);
            } else {
                Object result = invocation.proceed();
                future.complete(result);
            }
        } catch (Throwable e) {
            fLogger.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
