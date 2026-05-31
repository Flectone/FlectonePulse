package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Times;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.serializer.HytaleComponentSerializer;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleTitleRender implements TitleRender {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final HytaleComponentSerializer componentSerializer;

    @Override
    public void render(FPlayer fPlayer, Component title, Component subTitle, Times times) {
        Object object = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (!(object instanceof PlayerRef playerRef)) return;

        EventTitleUtil.showEventTitleToPlayer(playerRef,
                componentSerializer.toHytale(title),
                componentSerializer.toHytale(subTitle),
                true,
                null,
                (float) times.stayTicks() / 20,
                (float) times.fadeInTicks() / 20,
                (float) times.fadeOutTicks() / 20
        );

    }

}
