package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Times;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TitleRender {

    private final PacketProvider packetProvider;

    public void render(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        user.sendTitle(title, subTitle, times.fadeInTicks(), times.stayTicks(), times.fadeOutTicks());
    }

}
