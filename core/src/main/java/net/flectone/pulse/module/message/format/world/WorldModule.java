package net.flectone.pulse.module.message.format.world;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.world.listener.WorldPacketListener;
import net.flectone.pulse.module.message.format.world.listener.WorldPulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorldModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final PacketProvider packetProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable() || packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_9)) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::update), ticker.period());
        }

        listenerRegistry.register(WorldPacketListener.class);
        listenerRegistry.register(WorldPulseListener.class);
    }

    @Override
    public Message.Format.World config() {
        return fileFacade.message().format().world();
    }

    @Override
    public Permission.Message.Format.World permission() {
        return fileFacade.permission().message().format().world();
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;
        if (!messageContext.message().contains(MessagePipeline.ReplacementTag.WORLD_PREFIX.getTagName())) return messageContext;

        FEntity sender = messageContext.sender();
        if (isModuleDisabledFor(sender)) return messageContext;
        if (!(sender instanceof FPlayer fPlayer)) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.WORLD_PREFIX, (argumentQueue, context) -> {
            String worldPrefix = fPlayer.getSetting(SettingText.WORLD_PREFIX);
            if (worldPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(worldPrefix);
        });
    }

    @Async
    public void update(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        String newWorldPrefix = config().mode() == Mode.TYPE
                ? config().values().get(platformPlayerAdapter.getWorldEnvironment(fPlayer))
                : config().values().get(platformPlayerAdapter.getWorldName(fPlayer));

        String fPlayerWorldPrefix = fPlayer.getSetting(SettingText.WORLD_PREFIX);
        if (newWorldPrefix == null && fPlayerWorldPrefix == null) return;
        if (newWorldPrefix != null && newWorldPrefix.equalsIgnoreCase(fPlayerWorldPrefix)) return;

        fPlayer.setSetting(SettingText.WORLD_PREFIX, newWorldPrefix);
        fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.WORLD_PREFIX);
    }

    public enum Mode {
        TYPE,
        NAME
    }
}
