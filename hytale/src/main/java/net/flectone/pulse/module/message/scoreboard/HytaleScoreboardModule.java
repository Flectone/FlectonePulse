package net.flectone.pulse.module.message.scoreboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HytaleScoreboardModule extends ScoreboardModule {

    private final Map<UUID, CustomName> teamMap = new ConcurrentHashMap<>();

    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModuleController moduleController;
    private final ComponentSerializer componentSerializer;

    @Inject
    public HytaleScoreboardModule(FileFacade fileFacade,
                                  ListenerRegistry listenerRegistry,
                                  TaskScheduler taskScheduler,
                                  MessagePipeline messagePipeline,
                                  PlatformPlayerAdapter platformPlayerAdapter,
                                  ModuleController moduleController,
                                  ComponentSerializer componentSerializer,
                                  SocialService socialService) {
        super(fileFacade, listenerRegistry, platformPlayerAdapter, socialService);

        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.moduleController = moduleController;
        this.componentSerializer = componentSerializer;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(fPlayer -> {
                if (!teamMap.containsKey(fPlayer.uuid())) return;

                CustomName customName = createNameplate(fPlayer);
                sendPacket(fPlayer.uuid(), customName.value());

                teamMap.put(fPlayer.uuid(), customName);

            }, ticker.period());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        teamMap.forEach((uuid, customName) -> sendPacket(uuid, customName.original()));
        teamMap.clear();
    }

    @Override
    public void createOrUpdate(@NonNull FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            CustomName customName = createNameplate(fPlayer);
            sendPacket(fPlayer.uuid(), customName.value());

            teamMap.put(fPlayer.uuid(), customName);
        });
    }

    @Override
    public void remove(@NonNull FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            CustomName customName = teamMap.get(fPlayer.uuid());
            if (customName == null) return;

            teamMap.remove(fPlayer.uuid());
            sendPacket(fPlayer.uuid(), customName.original());
        });
    }

    private void sendPacket(@NonNull UUID uuid, @NonNull String newName) {
        if (!(platformPlayerAdapter.convertToPlatformPlayer(uuid) instanceof PlayerRef playerRef)) return;

        Ref<EntityStore> storeRef = playerRef.getReference();
        if (storeRef == null) return;

        storeRef.getStore().getExternalData().getWorld().execute(() -> {
            if (!storeRef.isValid()) return;

            Nameplate nameplate = storeRef.getStore().getComponent(playerRef.getReference(), Nameplate.getComponentType());
            if (nameplate != null) {
                nameplate.setText(newName);
            }
        });
    }

    @NonNull
    private CustomName createNameplate(@NonNull FPlayer fPlayer) {
        if (isInvisibleNameFor(fPlayer)) return new CustomName(fPlayer.name(), "");

        Component displayName = Component.text(fPlayer.name());

        Component prefix = Component.empty();
        if (!localization().prefix().isEmpty()) {
            prefix = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .message(localization().prefix())
                    .flag(MessageFlag.INVISIBLE_NAME_DETECTION, false)
                    .build()
            );
        }

        Component suffix = Component.empty();
        if (!localization().suffix().isEmpty()) {
            suffix = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .message(localization().suffix())
                    .flag(MessageFlag.INVISIBLE_NAME_DETECTION, false)
                    .build()
            );
        }

        return new CustomName(fPlayer.name(), componentSerializer.toPlain(prefix.append(displayName).append(suffix)));
    }

    private record CustomName(String original, String value) {
    }
}
