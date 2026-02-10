package net.flectone.pulse.platform.render;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.TextScreen;
import net.flectone.pulse.model.util.Times;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleTextScreenRender implements TextScreenRender {

    private final TitleRender titleRender;

    @Override
    public void clear() {
        // nothing
    }

    @Override
    public void render(FPlayer fPlayer, Component message, TextScreen textScreen) {
        titleRender.render(fPlayer, message, Component.empty(), new Times(1, 5, 1));
    }

    @Override
    public List<Integer> getPassengers(UUID uuid) {
        return Collections.emptyList();
    }

    @Override
    public void ride(UUID uuid, int playerId, List<Integer> textScreenPassengers, boolean silent) {
        // nothing
    }

    @Override
    public void updateAndRide(int playerId) {
        // nothing
    }

}
