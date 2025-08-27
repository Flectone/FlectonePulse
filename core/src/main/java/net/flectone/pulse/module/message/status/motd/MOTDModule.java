package net.flectone.pulse.module.message.status.motd;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.util.constant.MessageType;

import java.util.List;

@Singleton
public class MOTDModule extends AbstractModuleListLocalization<Localization.Message.Status.MOTD> {

    private final Message.Status.MOTD message;
    private final Permission.Message.Status.MOTD permission;
    private final MessagePipeline messagePipeline;
    private final PacketProvider packetProvider;

    @Inject
    public MOTDModule(FileResolver fileResolver,
                      MessagePipeline messagePipeline,
                      PacketProvider packetProvider) {
        super(localization -> localization.getMessage().getStatus().getMotd(), MessageType.MOTD);

        this.message = fileResolver.getMessage().getStatus().getMotd();
        this.permission = fileResolver.getPermission().getMessage().getStatus().getMotd();
        this.messagePipeline = messagePipeline;
        this.packetProvider = packetProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
    }


    public JsonElement next(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        String nextMessage = getNextMessage(fPlayer, this.message.isRandom());
        if (nextMessage == null) return null;

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
            return messagePipeline.builder(fPlayer, nextMessage).jsonSerializerBuild();
        } else {
            String serializedText = messagePipeline.builder(fPlayer, nextMessage).legacySerializerBuild();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("text", serializedText);
            return jsonObject;
        }
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return resolveLocalization(fPlayer).getValues();
    }
}
