package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.sender.PacketSender;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ListFooterRender {

    private final PacketSender packetSender;

    public void render(FPlayer fPlayer, Component header, Component footer) {
        packetSender.send(fPlayer, new WrapperPlayServerPlayerListHeaderAndFooter(header, footer));
    }

}
