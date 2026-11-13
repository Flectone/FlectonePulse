package net.flectone.pulse.module.message.serverlink;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerServerLinks;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerLinks;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.serverlink.listener.MinecraftPulseServerlinkListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

@Singleton
public class MinecraftServerlinkModule extends ServerlinkModuleImpl {

    private final Map<UUID, CachedLinks> cachedLinks = new ConcurrentHashMap<>();

    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final MinecraftPacketSender packetSender;
    private final MessagePipeline messagePipeline;
    private final MinecraftPacketProvider packetProvider;

    @Inject
    public MinecraftServerlinkModule(FileFacade fileFacade,
                                     SocialService socialService,
                                     ModuleController moduleController,
                                     ListenerRegistry listenerRegistry,
                                     TaskScheduler taskScheduler,
                                     MinecraftPacketSender packetSender,
                                     MessagePipeline messagePipeline,
                                     MinecraftPacketProvider packetProvider) {
        super(fileFacade, socialService);

        this.moduleController = moduleController;
        this.listenerRegistry = listenerRegistry;
        this.taskScheduler = taskScheduler;
        this.packetSender = packetSender;
        this.messagePipeline = messagePipeline;
        this.packetProvider = packetProvider;
    }

    @Override
    public void onEnable() {
        listenerRegistry.register(MinecraftPulseServerlinkListener.class);

        if (config().ticker().enable()) {
            taskScheduler.runPlayerAsyncTimer(this::sendLinks, config().ticker().period());
        }
    }

    @Override
    public void onDisable() {
        cachedLinks.clear();
    }

    @Override
    public BiPredicate<FEntity, Boolean> isDisable() {
        return super.isDisable().or((fEntity, _) -> {
            User user = packetProvider.getUser(fEntity.uuid());
            return user == null || user.getClientVersion().isOlderThan(ClientVersion.V_1_21);
        });
    }

    @Override
    public void sendLinks(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        Map<String, String> links = config().values();
        Map<String, String> linksMessages = localization(fPlayer).values();

        int hash = Objects.hash(links, linksMessages, fPlayer.name());
        long refreshTicks = config().refresh();
        long currentTick = taskScheduler.getCurrentTick();
        CachedLinks cached = cachedLinks.get(fPlayer.uuid());
        if (cached != null
                && cached.hash() == hash
                && (refreshTicks <= 0 || currentTick - cached.builtAtTick() < refreshTicks)) return;

        List<WrapperCommonServerServerLinks.ServerLink> serverLinks = links.entrySet().stream()
                .map(entry -> {
                    String link = entry.getValue();
                    Component linkComponent = messagePipeline.build(MessageContext.builder()
                            .module(this.name())
                            .sender(fPlayer)
                            .message(linksMessages.getOrDefault(entry.getKey(), link))
                            .build()
                    );

                    return new WrapperCommonServerServerLinks.ServerLink(linkComponent, link);
                }).toList();

        cachedLinks.put(fPlayer.uuid(), new CachedLinks(hash, currentTick));
        packetSender.send(fPlayer, new WrapperPlayServerServerLinks(serverLinks));
    }

    @Override
    public void remove(UUID uuid) {
        cachedLinks.remove(uuid);
    }

    private record CachedLinks(
            int hash,
            long builtAtTick
    ) {
    }

}
