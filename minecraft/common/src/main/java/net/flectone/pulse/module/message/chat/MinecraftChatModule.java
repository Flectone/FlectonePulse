package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatPacketListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftChatModule extends ChatModule {

    private final ListenerRegistry listenerRegistry;
    private final PlatformServerAdapter platformServerAdapter;

    @Inject
    public MinecraftChatModule(FileFacade fileFacade,
                               FPlayerService fPlayerService,
                               PlatformServerAdapter platformServerAdapter,
                               PermissionChecker permissionChecker,
                               IntegrationModule integrationModule,
                               Provider<BubbleModule> bubbleModuleProvider,
                               Provider<SpyModule> spyModuleProvider,
                               ListenerRegistry listenerRegistry,
                               TaskScheduler taskScheduler,
                               MuteSender muteSender,
                               DisableSender disableSender,
                               CooldownSender cooldownSender,
                               ProxyRegistry proxyRegistry) {
        super(fileFacade, fPlayerService, permissionChecker, integrationModule, bubbleModuleProvider, spyModuleProvider, taskScheduler, muteSender, disableSender, cooldownSender, proxyRegistry);

        this.listenerRegistry = listenerRegistry;
        this.platformServerAdapter = platformServerAdapter;
    }

    @Override
    public void onEnable() {
        if (config().mode() == Message.Chat.Mode.PACKET || platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            listenerRegistry.register(ChatPacketListener.class);
        }
    }

}
