package net.flectone.pulse.module.message.rightclick.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.rightclick.RightclickModule;

@Singleton
public class RightclickPacketListener extends AbstractPacketListener {

    private final RightclickModule rightClickModule;

    @Inject
    public RightclickPacketListener(RightclickModule rightClickModuleBukkit) {
        this.rightClickModule = rightClickModuleBukkit;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;

        WrapperPlayClientInteractEntity wrapperPlayClientInteractEntity = new WrapperPlayClientInteractEntity(event);

        if (wrapperPlayClientInteractEntity.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) return;

        rightClickModule.send(event.getUser().getUUID(), wrapperPlayClientInteractEntity.getEntityId());
    }
}
