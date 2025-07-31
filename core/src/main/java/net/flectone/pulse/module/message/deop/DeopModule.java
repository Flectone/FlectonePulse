package net.flectone.pulse.module.message.deop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.deop.listener.DeopPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.jetbrains.annotations.NotNull;

@Singleton
public class DeopModule extends AbstractModuleMessage<Localization.Message.Deop> {

    private final Message.Deop message;
    private final Permission.Message.Deop permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DeopModule(FileResolver fileResolver,
                      FPlayerService fPlayerService,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getDeop());

        this.message = fileResolver.getMessage().getDeop();
        this.permission = fileResolver.getPermission().getMessage().getDeop();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DeopPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, @NotNull String target) {
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(Localization.Message.Deop::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

}
