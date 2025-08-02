package net.flectone.pulse.module.message.format.moderation.caps;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.caps.listener.CapsPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class CapsModule extends AbstractModule {

    private final Message.Format.Moderation.Caps message;
    private final Permission.Message.Format.Moderation.Caps permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public CapsModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getModeration().getCaps();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getModeration().getCaps();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(CapsPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
