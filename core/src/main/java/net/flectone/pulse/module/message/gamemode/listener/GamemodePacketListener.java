package net.flectone.pulse.module.message.gamemode.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.gamemode.GamemodeModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class GamemodePacketListener extends AbstractPacketListener {

    private final GamemodeModule gamemodeModule;

    @Inject
    public GamemodePacketListener(GamemodeModule gamemodeModule) {
        this.gamemodeModule = gamemodeModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;

        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
        Component component = wrapper.getMessage();
        if (!(component instanceof TranslatableComponent translatableComponent)) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.gamemode.success")) return;
        if (translatableComponent.args().isEmpty()) return;
        if (!gamemodeModule.isEnable()) return;

        String target = event.getUser().getName();
        String gamemodeKey = "gameMode.survival";
        if (translatableComponent.args().get(0) instanceof TranslatableComponent gamemodeComponent) {
            gamemodeKey = gamemodeComponent.key();
        } else if (translatableComponent.args().size() > 1
                && translatableComponent.args().get(0) instanceof TextComponent playerComponent
                && translatableComponent.args().get(1) instanceof TranslatableComponent gamemodeComponent) {
            target = playerComponent.content();
            gamemodeKey = gamemodeComponent.key();
        }

        event.setCancelled(true);
        gamemodeModule.send(event.getUser().getUUID(), gamemodeKey, target);
    }
}
