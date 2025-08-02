package net.flectone.pulse.module.message.format.name;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.name.listener.NamePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class NameModule extends AbstractModuleLocalization<Localization.Message.Format.Name> {

    private final Message.Format.Name message;
    private final Permission.Message.Format.Name permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public NameModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat().getName_());

        this.message = fileResolver.getMessage().getFormat().getName_();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getName_();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(NamePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

}
