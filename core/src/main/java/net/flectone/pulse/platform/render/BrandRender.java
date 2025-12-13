package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.serializer.PacketSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BrandRender {

    private final PacketSender packetSender;
    private final PacketSerializer packetSerializer;

    public void render(FPlayer fPlayer, Component component) {
        String message = LegacyComponentSerializer.legacySection().serialize(component) + "§r";

        packetSender.send(fPlayer, new WrapperPlayServerPluginMessage(PacketSerializer.MINECRAFT_BRAND, packetSerializer.serialize(message)));
    }

}
