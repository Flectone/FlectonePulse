package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.sender.MessageSender;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalyActionBarRender implements ActionBarRender {

    private final MessageSender messageSender;

    @Override
    public void render(FPlayer fPlayer, Component component, int stayTicks) {
        messageSender.sendMessage(fPlayer, component, false);
    }

}
