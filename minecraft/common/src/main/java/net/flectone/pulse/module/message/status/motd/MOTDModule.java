package net.flectone.pulse.module.message.status.motd;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDModule extends AbstractModuleListLocalization<Localization.Message.Status.MOTD> {

    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;
    private final PacketProvider packetProvider;

    @Override
    public MessageType messageType() {
        return MessageType.MOTD;
    }

    @Override
    public Message.Status.MOTD config() {
        return fileFacade.message().status().motd();
    }

    @Override
    public Permission.Message.Status.MOTD permission() {
        return fileFacade.permission().message().status().motd();
    }

    @Override
    public Localization.Message.Status.MOTD localization(FEntity sender) {
        return fileFacade.localization(sender).message().status().motd();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return localization(fPlayer).values();
    }

    public JsonElement next(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        String nextMessage = getNextMessage(fPlayer, config().random());
        if (nextMessage == null) return null;

        MessageContext nextMessageContext = messagePipeline.createContext(fPlayer, nextMessage);
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
            return messagePipeline.buildJson(nextMessageContext);
        } else {
            String serializedText = messagePipeline.buildLegacy(nextMessageContext);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("text", serializedText);
            return jsonObject;
        }
    }
}
