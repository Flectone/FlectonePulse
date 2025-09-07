package net.flectone.pulse.module.message.summon;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.summon.listener.SummonPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class SummonModule extends AbstractModuleLocalization<Localization.Message.Summon> {

    private final Message.Summon message;
    private final Permission.Message.Summon permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SummonModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSummon(), MessageType.SUMMON);

        this.message = fileResolver.getMessage().getSummon();
        this.permission = fileResolver.getPermission().getMessage().getSummon();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SummonPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, FEntity entity) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(metadataBuilder()
                .sender(entity)
                .filterPlayer(fPlayer)
                .format(Localization.Message.Summon::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
