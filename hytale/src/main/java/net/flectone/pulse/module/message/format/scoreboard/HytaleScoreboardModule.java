package net.flectone.pulse.module.message.format.scoreboard;

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
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class HytaleScoreboardModule extends ScoreboardModule {

    private final Map<UUID, CustomName> uuidTeamMap = new ConcurrentHashMap<>();

    private record CustomName(String original, String value){}

    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public HytaleScoreboardModule(FileFacade fileFacade,
                                  ListenerRegistry listenerRegistry,
                                  TaskScheduler taskScheduler,
                                  MessagePipeline messagePipeline,
                                  PlatformPlayerAdapter platformPlayerAdapter) {
        super(fileFacade, listenerRegistry);
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(fPlayer -> {
                if (!uuidTeamMap.containsKey(fPlayer.uuid())) return;

                CustomName customName = createNameplate(fPlayer);
                sendPacket(fPlayer.uuid(), customName.value());

                uuidTeamMap.put(fPlayer.uuid(), customName);

            }, ticker.period());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        uuidTeamMap.forEach((uuid, customName) -> sendPacket(uuid, customName.original()));
        uuidTeamMap.clear();
    }

    @Override
    public void create(FPlayer fPlayer, boolean skipCacheTeam) {
        taskScheduler.runRegion(fPlayer, () -> {
            if (isModuleDisabledFor(fPlayer)) return;

            CustomName customName = createNameplate(fPlayer);
            sendPacket(fPlayer.uuid(), customName.value());

            uuidTeamMap.put(fPlayer.uuid(), customName);
        });
    }

    @Override
    public void remove(FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (isModuleDisabledFor(fPlayer)) return;

            CustomName customName = uuidTeamMap.get(fPlayer.uuid());
            if (customName == null) return;

            uuidTeamMap.remove(fPlayer.uuid());
            sendPacket(fPlayer.uuid(), customName.original());
        });
    }

    private void sendPacket(UUID uuid, String newName) {
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

    private CustomName createNameplate(FPlayer fPlayer) {
        // invisible name
        if (!config().nameVisible()) return new CustomName(fPlayer.name(), "");

        Component displayName = Component.text(fPlayer.name());

        Component prefix = Component.empty();
        if (!config().prefix().isEmpty()) {
            MessageContext prefixContext = messagePipeline.createContext(fPlayer, config().prefix())
                    .addFlag(MessageFlag.INVISIBLE_NAME, false);
            prefix = messagePipeline.build(prefixContext);
        }

        Component suffix = Component.empty();
        if (!config().suffix().isEmpty()) {
            MessageContext suffixContext = messagePipeline.createContext(fPlayer, config().suffix())
                    .addFlag(MessageFlag.INVISIBLE_NAME, false);
            suffix = messagePipeline.build(suffixContext);
        }

        return new CustomName(fPlayer.name(), PlainTextComponentSerializer.plainText().serialize(prefix.append(displayName).append(suffix)));
    }
}
