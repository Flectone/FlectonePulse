package net.flectone.pulse.module.message.status.motd;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.module.message.status.motd.listener.MOTDPacketListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.generator.RandomGenerator;
import net.flectone.pulse.platform.formatter.ServerStatusFormatter;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDModule implements ModuleListLocalization<Localization.Message.Status.MOTD> {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final RandomGenerator randomUtil;
    private final ServerStatusFormatter statusUtil;

    @Override
    public void onEnable() {
        listenerRegistry.register(MOTDPacketListener.class);
    }

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

    public void update(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SERVER_DATA) return;

        User user = event.getUser();

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        fPlayer = fPlayerService.loadColors(fPlayer);

        event.markForReEncode(true);

        WrapperPlayServerServerData wrapperPlayServerServerData = new WrapperPlayServerServerData(event);
        wrapperPlayServerServerData.setMOTD(next(fPlayer, user));
    }

    @Nullable
    public Component next(FPlayer fPlayer, User user) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        String nextMessage = getNextMessage(fPlayer, config().random());
        if (nextMessage == null) return null;

        return statusUtil.createMOTD(fPlayer, user, nextMessage);
    }

}
