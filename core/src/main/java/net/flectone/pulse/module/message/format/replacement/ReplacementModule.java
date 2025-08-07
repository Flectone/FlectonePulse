package net.flectone.pulse.module.message.format.replacement;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.replacement.listener.ReplacementPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class ReplacementModule extends AbstractModule {

    private final Message.Format.Replacement message;
    private final Permission.Message.Format.Replacement permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ReplacementModule(FileResolver fileResolver,
                             ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getReplacement();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getReplacement();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(ReplacementPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
