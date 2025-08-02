package net.flectone.pulse.module.message.format.style;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.style.listener.StylePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class StyleModule extends AbstractModule {

    private final Message.Format.Style message;
    private final Permission.Message.Format.Style permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public StyleModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getStyle();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getStyle();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(StylePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
