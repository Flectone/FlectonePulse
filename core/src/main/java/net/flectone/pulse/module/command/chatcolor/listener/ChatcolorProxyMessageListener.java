package net.flectone.pulse.module.command.chatcolor.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.module.command.chatcolor.ChatcolorModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;

import java.io.IOException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatcolorProxyMessageListener implements PulseListener {

    private final ChatcolorModule chatcolorModule;
    private final ModuleController moduleController;
    private final FPlayerService fPlayerService;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_CHATCOLOR) return event;
        if (!moduleController.isEnable(chatcolorModule)) return event.withProcessed(true);

        FPlayer fPlayer = fPlayerService.getFPlayer(event.sender());
        chatcolorModule.sendMessageWithUpdatedColors(fPlayer, event.uuid());

        return event.withProcessed(true);
    }

}
