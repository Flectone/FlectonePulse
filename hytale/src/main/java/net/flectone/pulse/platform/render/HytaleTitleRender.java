package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import eu.mikart.adventure.platform.hytale.HytaleComponentSerializer;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Times;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleTitleRender implements TitleRender {

    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public void render(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        Object object = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (!(object instanceof PlayerRef playerRef)) return;

        EventTitleUtil.showEventTitleToPlayer(playerRef,
                HytaleComponentSerializer.get().serialize(title),
                HytaleComponentSerializer.get().serialize(subTitle),
                true,
                null,
                times.stayTicks(),
                times.fadeInTicks(),
                times.fadeOutTicks()
        );

    }

}
