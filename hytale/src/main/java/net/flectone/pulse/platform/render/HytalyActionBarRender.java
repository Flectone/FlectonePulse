package net.flectone.pulse.platform.render;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.elements.LayoutModeSupported;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.HytaleMessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalyActionBarRender implements ActionBarRender {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final HytaleMessageUtil hytaleMessageUtil;

    @Override
    public void render(FPlayer fPlayer, Component component, int stayTicks) {
        if (!(platformPlayerAdapter.convertToPlatformPlayer(fPlayer) instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> storeRef = playerRef.getReference();
        if (storeRef == null) return;

        HudBuilder hudBuilder = HudBuilder.hudForPlayer(playerRef)
                .addElement(GroupBuilder.group()
                        .withLayoutMode(LayoutModeSupported.LayoutMode.CenterMiddle)
                        .withAnchor(new HyUIAnchor()
                                .setBottom(-700)
                        )
                        .addChild(LabelBuilder.label()
                                .withText(PlainTextComponentSerializer.plainText().serialize(component))
                                .withStyle(new HyUIStyle()
                                        .setTextColor(hytaleMessageUtil.findFirstColor(component))
                                )
                                .withPadding(new HyUIPadding().setFull(5))
                                .withBackground(new HyUIPatchStyle().setColor("#1E1E1E60"))
                        )
                );

        storeRef.getStore().getExternalData().getWorld().execute(() -> {
            HyUIHud hyUIHud = hudBuilder.show();
            taskScheduler.runAsyncLater(hyUIHud::remove, stayTicks + 20L); // +20 because minecraft fade_out ticks = 20
        });
    }

}
