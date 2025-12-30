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

/**
 * Renders server brand messages in player's client multiplayer screen.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * BrandRender brandRender = flectonePulse.get(BrandRender.class);
 *
 * // Display custom server brand
 * brandRender.render(player, Component.text("My Awesome Server"));
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BrandRender {

    private static final String RESET_STYLE = "§r";

    private final PacketSender packetSender;
    private final PacketSerializer packetSerializer;

    /**
     * Renders a server brand message to a player's client.
     *
     * @param fPlayer the player to receive the brand message
     * @param component the brand component to display
     */
    public void render(FPlayer fPlayer, Component component) {
        String message = LegacyComponentSerializer.legacySection().serialize(component) + RESET_STYLE;

        packetSender.send(fPlayer, new WrapperPlayServerPluginMessage(PacketSerializer.MINECRAFT_BRAND, packetSerializer.serialize(message)));
    }

}
