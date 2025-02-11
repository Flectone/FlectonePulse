package net.flectone.pulse.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DebugInterceptor implements MethodInterceptor {

    @Inject private FLogger fLogger;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();



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
            e.printStackTrace();
            fLogger.warning(e.getMessage());
        }
    }
}
