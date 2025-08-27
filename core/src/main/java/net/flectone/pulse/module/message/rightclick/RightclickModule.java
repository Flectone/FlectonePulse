package net.flectone.pulse.module.message.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.rightclick.listener.RightclickPacketListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;

import java.util.UUID;


@Singleton
public class RightclickModule extends AbstractModuleLocalization<Localization.Message.Rightclick> {

    private final Message.Rightclick message;
    private final Permission.Message.Rightclick permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public RightclickModule(FileResolver fileResolver,
                            FPlayerService fPlayerService,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getRightclick(), MessageType.RIGHT_CLICK);

        this.message = fileResolver.getMessage().getRightclick();
        this.permission = fileResolver.getPermission().getMessage().getRightclick();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        listenerRegistry.register(RightclickPacketListener.class);

        addPredicate(this::checkCooldown);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID uuid, int targetId) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        if (isModuleDisabledFor(fPlayer)) return;

        UUID targetUUID = platformPlayerAdapter.getPlayerByEntityId(targetId);
        if (targetUUID == null) return;

        FPlayer fTarget = fPlayerService.getFPlayer(targetUUID);
        if (fTarget.isUnknown()) return;

        sendMessage(metadataBuilder()
                .sender(fTarget)
                .receiver(fPlayer)
                .format(Localization.Message.Rightclick::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
