package net.flectone.pulse.platform.formatter;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftServerStatusFormatter {

    private static final String BASE64_IMAGE_HEADER = "data:image/png;base64,";

    private final MinecraftPacketProvider packetProvider;
    private final MessagePipeline messagePipeline;
    private final PlatformServerAdapter platformServerAdapter;
    private final ComponentSerializer componentSerializer;

    @NonNull
    public JsonElement formatDescription(FPlayer fPlayer, User user, String message) {
        return formatDescription(createMOTD(fPlayer, user, message));
    }

    @NonNull
    public Component createMOTD(FPlayer fPlayer, User user, String message) {
        // display player_head in MOTD is only available for clients 1.21.9-1.21.11
        if (user.getClientVersion().isOlderThan(ClientVersion.V_1_21_9)
                || user.getClientVersion().isNewerThan(ClientVersion.V_1_21_11)) {
            return messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .message(message)
                    .flag(MessageFlag.OBJECT_RECEIVER_VALIDATION, false)
                    .flag(MessageFlag.OBJECT_DEFAULT_VALUE, true)
                    .build()
            );
        }

        return messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .message(message)
                .flag(MessageFlag.OBJECT_RECEIVER_VALIDATION, false)
                .build()
        );
    }

    @NonNull
    public JsonElement formatDescription(@Nullable Component motd) {
        if (motd == null) return (JsonElement) platformServerAdapter.getMOTD();

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
            return (JsonElement) componentSerializer.toJsonTree(motd);
        } else {
            String serializedText =  componentSerializer.toLegacy(motd);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("text", serializedText);
            return jsonObject;
        }
    }

    @Nullable
    public String formatIcon(String icon) {
        if (icon == null) {
            String serverIcon = platformServerAdapter.getIcon();
            return serverIcon != null ? BASE64_IMAGE_HEADER + serverIcon : null;
        }

        return BASE64_IMAGE_HEADER + icon;
    }

}
