package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.fixation.listener.FixationPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class FixationModule extends AbstractModule {

    private final Message.Format.Fixation message;
    private final Permission.Message.Format.Fixation permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FixationModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getFixation();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getFixation();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(FixationPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
