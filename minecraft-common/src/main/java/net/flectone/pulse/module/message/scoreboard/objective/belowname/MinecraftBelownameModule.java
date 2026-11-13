package net.flectone.pulse.module.message.scoreboard.objective.belowname;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.message.scoreboard.objective.ObjectiveModule;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.listener.MinecraftPacketBelownameListener;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.listener.MinecraftPulseBelownameListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;

import java.util.List;
import java.util.UUID;

@Singleton
public class MinecraftBelownameModule extends BelownameModuleImpl {

    private static final int ATTRIBUTE_BASE_VALUE = 10;

    private final ListenerRegistry listenerRegistry;
    private final MinecraftPacketSender packetSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MinecraftPacketProvider packetProvider;
    private final boolean isNewerThanOrEqualsV262;

    @Inject
    public MinecraftBelownameModule(FileFacade fileFacade,
                                    SocialService socialService,
                                    FPlayerService fPlayerService,
                                    TaskScheduler taskScheduler,
                                    ModuleController moduleController,
                                    ObjectiveModule objectiveModule,
                                    ListenerRegistry listenerRegistry,
                                    MinecraftPacketSender packetSender,
                                    PlatformPlayerAdapter platformPlayerAdapter,
                                    MinecraftPacketProvider packetProvider,
                                    @Named("isNewerThanOrEqualsV_26_2") boolean isNewerThanOrEqualsV262) {
        super(fileFacade, socialService, fPlayerService, taskScheduler, moduleController, objectiveModule);

        this.listenerRegistry = listenerRegistry;
        this.packetSender = packetSender;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.packetProvider = packetProvider;
        this.isNewerThanOrEqualsV262 = isNewerThanOrEqualsV262;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPulseBelownameListener.class);

        if (isNewerThanOrEqualsV262) {
            listenerRegistry.register(MinecraftPacketBelownameListener.class);

            sendForAll(false);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (!FlectonePulseAPI.isDisabling() && isNewerThanOrEqualsV262) {
            sendForAll(true);
        }
    }

    public boolean isModernPlayer(UUID uuid) {
        User user = packetProvider.getUser(uuid);
        if (user == null) return false;

        return user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2);
    }

    public void sendForAll(boolean baseValue) {
        List<UUID> onlinePlayers = platformPlayerAdapter.getOnlinePlayers();
        onlinePlayers.forEach(player -> {
            int entityId = platformPlayerAdapter.getEntityId(player);
            if (entityId == 0) return;

            onlinePlayers.stream()
                    .filter(this::isModernPlayer)
                    .forEach(receiver -> send(receiver, entityId, baseValue));
        });
    }

    public void send(UUID uuid, int entityId, boolean baseValue) {
        packetSender.send(uuid, new WrapperPlayServerUpdateAttributes(
                entityId,
                List.of(new WrapperPlayServerUpdateAttributes.Property(Attributes.BELOW_NAME_DISTANCE, baseValue ? ATTRIBUTE_BASE_VALUE : config().distance(), List.of())))
        );
    }

}
