package net.flectone.pulse.platform.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class FabricProxySender extends ProxySender {

    @Inject
    public FabricProxySender(ProxyRegistry proxyRegistry, FileResolver fileResolver, MessagePipeline messagePipeline, Gson gson, FLogger fLogger) {
        super(proxyRegistry, fileResolver, messagePipeline, gson, fLogger);
    }

}
