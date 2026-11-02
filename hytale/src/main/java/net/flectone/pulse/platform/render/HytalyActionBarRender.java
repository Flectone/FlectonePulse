package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.HytaleServer;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalyActionBarRender implements ActionBarRender {

    private final HytaleHudService hudService;

    @Override
    public void render(FPlayer fPlayer, Component component, int stayTicks) {
        long actionBarId = hudService.showActionBar(fPlayer, component);

        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> hudService.hideActionBar(fPlayer, actionBarId),
                (stayTicks + 20L) * 50,
                TimeUnit.MILLISECONDS
        );
    }

}
