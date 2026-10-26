package net.flectone.pulse.module.command.nickname.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.entity.FPlayerImpl;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.command.nickname.NicknameModule;
import net.flectone.pulse.util.payload.ProxyPayload;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NicknameProxyMessageListener implements PulseListener {

    private final Gson gson;
    private final NicknameModule nicknameModule;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_NICKNAME) return event;

        try (ProxyPayload proxyPayload = event.openPayload()) {
            FPlayer fPlayer = gson.fromJson(proxyPayload.readString(), FPlayerImpl.class);
            String nickname = proxyPayload.readString();

            nicknameModule.sendMessageWithUpdatedNickname(fPlayer, event.sender(), nickname, event.uuid());
        }

        return event.withProcessed(true);
    }

}
