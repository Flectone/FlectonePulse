package net.flectone.pulse.module.message.sidebar;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.elements.LayoutModeSupported;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.HytaleMessageUtil;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HytaleSidebarModule extends SidebarModule {

    private final Map<UUID, HyUIHud> playerSidebars = new ConcurrentHashMap<>();

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final HytaleMessageUtil hytaleMessageUtil;

    @Inject
    public HytaleSidebarModule(FileFacade fileFacade,
                               TaskScheduler taskScheduler,
                               ListenerRegistry listenerRegistry,
                               FPlayerService fPlayerService,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PermissionChecker permissionChecker,
                               MessagePipeline messagePipeline,
                               HytaleMessageUtil hytaleMessageUtil) {
        super(fileFacade, taskScheduler, listenerRegistry, fPlayerService);

        this.platformPlayerAdapter = platformPlayerAdapter;
        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.hytaleMessageUtil = hytaleMessageUtil;
    }

    @Override
    public void remove(FPlayer fPlayer) {
        HyUIHud hyUIHud = playerSidebars.get(fPlayer.uuid());
        if (hyUIHud != null)  {
            hyUIHud.remove();

            playerSidebars.remove(fPlayer.uuid());
        }
    }

    @Override
    public void update(FPlayer fPlayer) {
        if (!playerSidebars.containsKey(fPlayer.uuid())) {
            create(fPlayer);
        }

        HyUIHud hyUIHud = playerSidebars.get(fPlayer.uuid());
        if (hyUIHud == null) return;
        if (!(platformPlayerAdapter.convertToPlatformPlayer(fPlayer) instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        HudBuilder hudBuilder = createHudBuilder(fPlayer, playerRef);
        if (hudBuilder == null) return;

        refStore.getStore().getExternalData().getWorld().execute(() -> hyUIHud.update(hudBuilder));
    }

    @Override
    public void create(FPlayer fPlayer) {
        if (!(platformPlayerAdapter.convertToPlatformPlayer(fPlayer) instanceof PlayerRef playerRef)) return;

        remove(fPlayer);

        if (!permissionChecker.check(fPlayer, permission())) {
            remove(fPlayer);
            return;
        }

        if (isModuleDisabledFor(fPlayer)) return;

        Ref<EntityStore> refStore = playerRef.getReference();
        if (refStore == null) return;

        HudBuilder hudBuilder = createHudBuilder(fPlayer, playerRef);
        if (hudBuilder == null) return;

        refStore.getStore().getExternalData().getWorld().execute(() -> playerSidebars.put(fPlayer.uuid(), hudBuilder.show()));
    }

    private HudBuilder createHudBuilder(FPlayer fPlayer, PlayerRef playerRef) {
        String format = getNextMessage(fPlayer, config().random());
        if (format == null) return null;

        String[] lines = format.split("<br>");
        if (lines.length == 0) return null;

        String objectiveName = getObjectiveName(fPlayer);

        GroupBuilder lineBuilder = GroupBuilder.group()
                .withId(objectiveName)
                .withLayoutMode(LayoutModeSupported.LayoutMode.Top);

        for (int i = 0; i < lines.length; i++) {
            String lineId = getLineId(i, fPlayer);
            MessageContext lineContext = messagePipeline.createContext(fPlayer, lines[i]);
            Component line = messagePipeline.build(lineContext);

            lineBuilder.addChild(LabelBuilder.label()
                    .withId(lineId)
                    .withText(PlainTextComponentSerializer.plainText().serialize(line))
                    .withStyle(new HyUIStyle().setTextColor(hytaleMessageUtil.findFirstColor(line)))
                    .withPadding(new HyUIPadding(config().labelLeft(), config().labelTop(), 0, 0))
            );
        }

        return HudBuilder.hudForPlayer(playerRef)
                .addElement(GroupBuilder.group()
                        .withBackground(new HyUIPatchStyle()
                                .setColor(config().background())
                        )
                        .withAnchor(new HyUIAnchor()
                                .setWidth(config().width())
                                .setHeight(config().height())
                                .setTop(config().top())
                                .setRight(config().right())
                        )
                        .addChild(lineBuilder)
                );
    }

}
