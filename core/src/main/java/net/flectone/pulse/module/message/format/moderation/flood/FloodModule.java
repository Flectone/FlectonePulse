package net.flectone.pulse.module.message.format.moderation.flood;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.flood.listener.FloodPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class FloodModule extends AbstractModule {

    private final Message.Format.Moderation.Flood message;
    private final Permission.Message.Format.Moderation.Flood permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FloodModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getFlood();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getFlood();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(FloodPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
