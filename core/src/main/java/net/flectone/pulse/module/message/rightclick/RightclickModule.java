package net.flectone.pulse.module.message.rightclick;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.rightclick.listener.RightclickPacketListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;

import java.util.UUID;


@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RightclickModule extends AbstractModuleLocalization<Localization.Message.Rightclick> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().sound(), permission().sound());
        createCooldown(config().cooldown(), permission().cooldownBypass());

        listenerRegistry.register(RightclickPacketListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.RIGHT_CLICK;
    }

    @Override
    public Message.Rightclick config() {
        return fileFacade.message().rightclick();
    }

    @Override
    public Permission.Message.Rightclick permission() {
        return fileFacade.permission().message().rightclick();
    }

    @Override
    public Localization.Message.Rightclick localization(FEntity sender) {
        return fileFacade.localization(sender).message().rightclick();
    }

    @Async
    public void send(UUID uuid, int targetId) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        if (isModuleDisabledFor(fPlayer)) return;

        UUID targetUUID = platformPlayerAdapter.getPlayerByEntityId(targetId);
        if (targetUUID == null) return;

        FPlayer fTarget = fPlayerService.getFPlayer(targetUUID);
        if (fTarget.isUnknown()) return;
        if (config().shouldCheckSneaking() && !platformPlayerAdapter.isSneaking(fPlayer)) return;
        if (config().hideNameWhenInvisible() && platformPlayerAdapter.hasPotionEffect(fTarget, PotionTypes.INVISIBILITY)) return;

        sendMessage(metadataBuilder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(Localization.Message.Rightclick::format)
                .destination(config().destination())
                .sound(getModuleSound())
                .build()
        );
    }
}
