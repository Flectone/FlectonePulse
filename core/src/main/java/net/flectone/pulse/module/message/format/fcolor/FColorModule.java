package net.flectone.pulse.module.message.format.fcolor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.fcolor.listener.FColorPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;


@Singleton
public class FColorModule extends AbstractModule {

    private final Message.Format.FColor message;
    private final Permission.Message.Format.FColor permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FColorModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getFcolor();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getFcolor();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        // register fColor types
        permission.getColors().forEach((key, value) -> registerPermission(value));

        listenerRegistry.register(FColorPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
