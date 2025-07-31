package net.flectone.pulse.module.message.sign;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.sign.listener.SignListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class BukkitSignModule extends SignModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitSignModule(FileResolver fileResolver,
                            MessagePipeline messagePipeline,
                            ListenerRegistry listenerRegistry) {
        super(fileResolver, messagePipeline);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(SignListener.class);
    }
}
