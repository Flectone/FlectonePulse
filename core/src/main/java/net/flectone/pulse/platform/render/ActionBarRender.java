package net.flectone.pulse.platform.render;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Renders action bar messages to players with version compatibility.
 * Supports all Minecraft versions from 1.8 to latest.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * ActionBarRender actionBar = flectonePulse.get(ActionBarRender.class);
 *
 * // Send a temporary action bar
 * actionBar.render(player, Component.text("Hello!"), 60); // 3 seconds
 * }</pre>
 *
 * @since 1.7.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActionBarRender {

    private final PacketProvider packetProvider;
    private final PacketSender packetSender;
    private final TaskScheduler taskScheduler;

    /**
     * Renders an action bar message with default duration (30 ticks).
     *
     * @param fPlayer the player to receive the action bar
     * @param component the message component to display
     */
    public void render(FPlayer fPlayer, Component component) {
        render(fPlayer, component, 0);
    }

    /**
     * Renders an action bar message with custom duration.
     *
     * @param fPlayer the player to receive the action bar
     * @param component the message component to display
     * @param stayTicks duration in ticks (20 ticks = 1 second, 0 = default)
     */
    public void render(FPlayer fPlayer, Component component, int stayTicks) {
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) {
            packetSender.send(fPlayer, new WrapperPlayServerSystemChatMessage(true, component));
        } else if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
            packetSender.send(fPlayer, new WrapperPlayServerActionBar(component));
        } else if (packetProvider.getServerVersion().isNewerThan(ServerVersion.V_1_16)) {
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessage_v1_16(component, ChatTypes.GAME_INFO, fPlayer.getUuid())));
        } else if (packetProvider.getServerVersion().isNewerThan(ServerVersion.V_1_8_8)) {
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessageLegacy(component, ChatTypes.GAME_INFO)));
        } else { // PacketEvents issue https://github.com/retrooper/packetevents/issues/1241
            packetSender.send(fPlayer, new WrapperPlayServerChatMessage(new ChatMessageLegacy(Component.text(LegacyComponentSerializer.legacySection().serialize(component)), ChatTypes.GAME_INFO)));
        }

        // cannot set stay ticks for action bar, so
        if (stayTicks <= 30) return;

        int remainingTicks = stayTicks - 30;
        int delay = Math.min(30, remainingTicks);

        taskScheduler.runAsyncLater(() -> render(fPlayer, component, remainingTicks), delay);
    }

}
