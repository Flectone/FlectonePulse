package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.processing.serializer.BrandPacketSerializer;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftBrandRender implements BrandRender {

    private static final String RESET_STYLE = "§r";

    private final MinecraftPacketSender packetSender;
    private final BrandPacketSerializer brandPacketSerializer;
    private final ComponentSerializer componentSerializer;

    @Override
    public void render(FPlayer fPlayer, Component component) {
        String message = componentSerializer.toLegacy(component) + RESET_STYLE;

        packetSender.send(fPlayer, new WrapperPlayServerPluginMessage(BrandPacketSerializer.MINECRAFT_BRAND, brandPacketSerializer.serialize(message)));
    }

}
