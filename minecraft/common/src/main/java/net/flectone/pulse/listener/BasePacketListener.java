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
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.render.TextScreenRender;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.processor.PlayerPreLoginProcessor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasePacketListener implements PacketListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final EventDispatcher eventDispatcher;
    private final PacketProvider packetProvider;
    private final PacketSender packetSender;
    private final PlayerPreLoginProcessor playerPreLoginProcessor;
    private final TextScreenRender textScreenRender;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;

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

        if (wrapperLocale.equals(fPlayer.getSetting(SettingText.LOCALE))) return;
        if (fPlayerService.updateLocale(fPlayer, wrapperLocale)) return;

        // first time player joined, wait for it to be added
        fPlayerService.updateLocaleLater(uuid, wrapperLocale);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        boolean usePacketPreLoginListener =
                // only for 1.20.2 and newer versions
                // because there is a configuration stage and there are no problems with evet.setСancelled(True)
                event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS && packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_2)
                // or maybe it's enabled in config, it only works for Bukkit
                && !(platformServerAdapter.getPlatformType() == PlatformType.BUKKIT && fileFacade.config().module().useBukkitPreLoginListener());

        if (usePacketPreLoginListener) {
            WrapperLoginServerLoginSuccess wrapper = new WrapperLoginServerLoginSuccess(event);
            UserProfile userProfile = wrapper.getUserProfile();

            UUID uuid = userProfile.getUUID();
            if (uuid == null) return;

            String playerName = userProfile.getName();
            if (playerName == null) return;

            event.setCancelled(true);

            playerPreLoginProcessor.processAsyncLogin(uuid, playerName,
                    loginEvent -> packetSender.send(uuid, new WrapperLoginServerLoginSuccess(uuid, playerName)),
                    loginEvent -> packetSender.send(uuid, new WrapperLoginServerDisconnect(loginEvent.kickReason()))
            );
        }

        if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
            WrapperPlayServerSetPassengers wrapper = new WrapperPlayServerSetPassengers(event);
            textScreenRender.updateAndRide(wrapper.getEntityId());
            return;
        }

        Optional<Pair<Component, Boolean>> optionalPair = toMessageReceiveEvent(event);
        if (optionalPair.isEmpty()) return;

        User user = event.getUser();
        if (user == null) return;

        UUID userUUID = user.getUUID();
        if (userUUID == null) return;

        Pair<Component, Boolean> triplet = optionalPair.get();

        // skip minecraft warning
        if (triplet.getLeft() instanceof TranslatableComponent translatableComponent && translatableComponent.key().equals("multiplayer.message_not_delivered")) {
            event.setCancelled(true);
            return;
        }

        FPlayer fPlayer = fPlayerService.getFPlayer(userUUID);
        MessageReceiveEvent messageReceiveEvent = eventDispatcher.dispatch(new MessageReceiveEvent(fPlayer, triplet.getLeft(), triplet.getRight()));

        event.setCancelled(messageReceiveEvent.cancelled());
    }

    private Optional<Pair<Component, Boolean>> toMessageReceiveEvent(PacketSendEvent event) {
        Component component = null;
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

        if (component != null) {
            return Optional.of(Pair.of(component, overlay));
        }

        return Optional.empty();
    }
}
