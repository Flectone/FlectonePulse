package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.incendo.cloud.type.tuple.Triplet;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class BasePacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final PacketProvider packetProvider;
    private final PacketSender packetSender;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final FLogger fLogger;

    @Inject
    public BasePacketListener(FPlayerService fPlayerService,
                              EventDispatcher eventDispatcher,
                              PacketProvider packetProvider,
                              PacketSender packetSender,
                              PlayerPreLoginProcessor playerPreLoginProcessor,
                              FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.eventDispatcher = eventDispatcher;
        this.packetProvider = packetProvider;
        this.packetSender = packetSender;
        this.playerPreLoginProcessor = playerPreLoginProcessor;
        this.fLogger = fLogger;
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

        Optional<Triplet<Component, MinecraftTranslationKey, Boolean>> optionalTriplet = toMessageReceiveEvent(event);
        if (optionalTriplet.isEmpty()) return;

        User user = event.getUser();
        if (user == null) return;

        UUID userUUID = user.getUUID();
        if (userUUID == null) return;

        Triplet<Component, MinecraftTranslationKey, Boolean> triplet = optionalTriplet.get();

        // skip minecraft warning
        if (triplet.second() == MinecraftTranslationKey.MULTIPLAYER_MESSAGE_NOT_DELIVERED) {
            event.setCancelled(true);
            return;
        }

        FPlayer fPlayer = fPlayerService.getFPlayer(userUUID);
        MessageReceiveEvent messageReceiveEvent = new MessageReceiveEvent(fPlayer, triplet);

        eventDispatcher.dispatch(messageReceiveEvent);
        event.setCancelled(messageReceiveEvent.isCancelled());
    }

    private Optional<Triplet<Component, MinecraftTranslationKey, Boolean>> toMessageReceiveEvent(PacketSendEvent event) {
        Component component = null;
        MinecraftTranslationKey translationKey = MinecraftTranslationKey.UNKNOWN;
        boolean overlay = false;

        try {
            if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
                WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);
                component = wrapper.getMessage().getChatContent();
            } else if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
                WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
                component = wrapper.getMessage();
                overlay = wrapper.isOverlay();
            }
        } catch (Exception e) {
            fLogger.warning("Error when reading a PacketType.Play.Server." + event.getPacketType() + ", THIS IS NOT A FLECTONEPULSE BUG, Report to PacketEvents: " + e.getMessage());
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            translationKey = MinecraftTranslationKey.fromString(translatableComponent);
        }

        if (component != null) {
            return Optional.of(Triplet.of(component, translationKey, overlay));
        }

        return Optional.empty();
    }
}
