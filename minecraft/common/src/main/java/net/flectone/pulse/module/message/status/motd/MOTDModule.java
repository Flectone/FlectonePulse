package net.flectone.pulse.module.message.status.motd;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
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
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDModule implements ModuleListLocalization<Localization.Message.Status.MOTD> {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;
    private final PacketProvider packetProvider;
    private final ModuleController moduleController;
    private final RandomUtil randomUtil;

    @Override
    public void onDisable() {
        messageIndexMap.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_STATUS_MOTD;
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

    @Override
    public int getPlayerIndexOrDefault(int id, int defaultIndex) {
        return messageIndexMap.getOrDefault(id, defaultIndex);
    }

    @Override
    public int nextInt(int start, int end) {
        return randomUtil.nextInt(start, end);
    }

    @Override
    public void savePlayerIndex(int id, int playerIndex) {
        messageIndexMap.put(id, playerIndex);
    }

    public JsonElement next(FPlayer fPlayer, User user) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        String nextMessage = getNextMessage(fPlayer, config().random());
        if (nextMessage == null) return null;

        MessageContext nextMessageContext = messagePipeline.createContext(fPlayer, nextMessage)
                .addFlag(MessageFlag.OBJECT_RECEIVER_VALIDATION, false);

        if (user.getClientVersion().isOlderThan(ClientVersion.V_1_21_9)) {
            nextMessageContext = nextMessageContext.addFlag(MessageFlag.OBJECT_DEFAULT_VALUE, true);
        }

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
