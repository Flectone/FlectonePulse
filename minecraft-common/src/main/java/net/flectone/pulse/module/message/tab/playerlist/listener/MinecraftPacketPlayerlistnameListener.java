package net.flectone.pulse.module.message.tab.playerlist.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketPlayerlistnameListener implements PacketListener {

    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> OVERWRITING_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER
    );

    private final MinecraftPlayerlistnameModule playerlistnameModule;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        UUID receiver = event.getUser().getUUID();
        if (receiver == null) return;

        switch (event.getPacketType()) {
            case PacketType.Play.Server.PLAYER_INFO_UPDATE -> {
                WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(event);
                if (Collections.disjoint(wrapper.getActions(), OVERWRITING_ACTIONS)) return;

                wrapper.getEntries().forEach(playerInfo -> forgetEntry(receiver, playerInfo.getGameProfile()));
            }
            case PacketType.Play.Server.PLAYER_INFO_REMOVE -> {
                WrapperPlayServerPlayerInfoRemove wrapper = new WrapperPlayServerPlayerInfoRemove(event);

                wrapper.getProfileIds().forEach(uuid -> playerlistnameModule.forgetEntry(receiver, uuid));
            }
            case PacketType.Play.Server.PLAYER_INFO -> {
                WrapperPlayServerPlayerInfo wrapper = new WrapperPlayServerPlayerInfo(event);
                if (!isOverwriting(wrapper.getAction())) return;

                wrapper.getPlayerDataList().forEach(playerData -> forgetEntry(receiver, playerData.getUserProfile()));
            }
            default -> {
            }
        }
    }

    private boolean isOverwriting(WrapperPlayServerPlayerInfo.@Nullable Action action) {
        if (action == null) return false;

        return switch (action) {
            case ADD_PLAYER, UPDATE_DISPLAY_NAME, REMOVE_PLAYER -> true;
            case UPDATE_GAME_MODE, UPDATE_LATENCY -> false;
        };
    }

    private void forgetEntry(UUID receiver, @Nullable UserProfile userProfile) {
        if (userProfile == null) return;

        playerlistnameModule.forgetEntry(receiver, userProfile.getUUID());
    }

}
