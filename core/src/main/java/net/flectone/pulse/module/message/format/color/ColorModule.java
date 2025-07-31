package net.flectone.pulse.module.message.format.color;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.color.listener.ColorPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;


@Singleton
public class ColorModule extends AbstractModule {

    private final Message.Format.Color message;
    private final Permission.Message.Format.Color permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ColorModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getColor();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getColor();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(ColorPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
