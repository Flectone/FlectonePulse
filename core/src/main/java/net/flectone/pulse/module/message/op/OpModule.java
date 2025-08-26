package net.flectone.pulse.module.message.op;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.op.listener.OpPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class OpModule extends AbstractModuleLocalization<Localization.Message.Op> {

    private final Message.Op message;
    private final Permission.Message.Op permission;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public OpModule(FileResolver fileResolver,
                    FPlayerService fPlayerService,
                    ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getOp(), MessageType.OP);

        this.message = fileResolver.getMessage().getOp();
        this.permission = fileResolver.getPermission().getMessage().getOp();
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(OpPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, String target) {
        if (isModuleDisabledFor(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(target);
        if (fTarget.isUnknown()) return;

        sendMessage(metadataBuilder()
                .sender(fTarget)
                .receiver(fPlayer)
                .format(Localization.Message.Op::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
