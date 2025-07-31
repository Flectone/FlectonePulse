package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.UUID;

@Singleton
public class BasePacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final PacketProvider packetProvider;
    private final PacketSender packetSender;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;

    @Inject
    public BasePacketListener(FPlayerService fPlayerService,
                              EventDispatcher eventDispatcher,
                              PacketProvider packetProvider,
                              PacketSender packetSender,
                              PlayerPreLoginProcessor playerPreLoginProcessor) {
        this.fPlayerService = fPlayerService;
        this.eventDispatcher = eventDispatcher;
        this.packetProvider = packetProvider;
        this.packetSender = packetSender;
        this.playerPreLoginProcessor = playerPreLoginProcessor;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType != PacketType.Play.Client.CLIENT_SETTINGS
                && packetType != PacketType.Configuration.Client.CLIENT_SETTINGS) return;

        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        WrapperPlayClientSettings wrapperPlayClientSettings = new WrapperPlayClientSettings(event);
        String wrapperLocale = wrapperPlayClientSettings.getLocale();

        fPlayerService.updateLocale(fPlayer, wrapperLocale);

        // first time player joined, wait for it to be added
        fPlayerService.updateLocaleLater(uuid, wrapperLocale);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        // only for 1.20.2 and newer versions
        // because there is a configuration stage and there are no problems with evet.setСancelled(True)
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS
                && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)) {

            WrapperLoginServerLoginSuccess wrapper = new WrapperLoginServerLoginSuccess(event);
            UserProfile userProfile = wrapper.getUserProfile();

            UUID uuid = userProfile.getUUID();
            if (uuid == null) return;

            String playerName = userProfile.getName();
            if (playerName == null) return;

            event.setCancelled(true);

            playerPreLoginProcessor.processAsyncLogin(uuid, playerName,
                    loginEvent -> packetSender.send(uuid, new WrapperLoginServerLoginSuccess(uuid, playerName)),
                    loginEvent -> packetSender.send(uuid, new WrapperLoginServerDisconnect(loginEvent.getKickReason()))
            );
        }

        TranslatableComponent translatableComponent = parseTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKey key = MinecraftTranslationKey.fromString(translatableComponent.key());

        // skip minecraft warning
        if (key == MinecraftTranslationKey.MULTIPLAYER_MESSAGE_NOT_DELIVERED) {
            event.setCancelled(true);
            return;
        }

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());
        eventDispatcher.dispatch(new TranslatableMessageReceiveEvent(fPlayer, key, translatableComponent, event));
    }

    private TranslatableComponent parseTranslatableComponent(PacketSendEvent event) {
        Component component = null;

        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
            WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);
            component = wrapper.getMessage().getChatContent();
        } else if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
            component = wrapper.getMessage();
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            return translatableComponent;
        }

        return null;
    }
}
