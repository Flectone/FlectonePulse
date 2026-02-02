package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.BossBar;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleBossBarRender implements BossBarRender {

    @Override
    public void render(FPlayer fPlayer, Component component, BossBar bossBar) {
        // not supported
    }

}
