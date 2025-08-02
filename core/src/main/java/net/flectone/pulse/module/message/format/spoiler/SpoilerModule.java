package net.flectone.pulse.module.message.format.spoiler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.format.spoiler.listener.SpoilerPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class SpoilerModule extends AbstractModuleLocalization<Localization.Message.Format.Spoiler> {

    private final Message.Format.Spoiler message;
    private final Permission.Message.Format.Spoiler permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SpoilerModule(FileResolver fileResolver,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getFormat().getSpoiler());

        this.message = fileResolver.getMessage().getFormat().getSpoiler();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getSpoiler();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(SpoilerPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
