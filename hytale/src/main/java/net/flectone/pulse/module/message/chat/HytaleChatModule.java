package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatHytaleListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class HytaleChatModule extends ChatModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public HytaleChatModule(FileFacade fileFacade,
                            FPlayerService fPlayerService,
                            PermissionChecker permissionChecker,
                            IntegrationModule integrationModule,
                            Provider<BubbleModule> bubbleModuleProvider,
                            Provider<SpyModule> spyModuleProvider,
                            TaskScheduler taskScheduler,
                            MuteSender muteSender,
                            DisableSender disableSender,
                            CooldownSender cooldownSender,
                            ProxyRegistry proxyRegistry,
                            ListenerRegistry listenerRegistry) {
        super(fileFacade, fPlayerService, permissionChecker, integrationModule, bubbleModuleProvider, spyModuleProvider, taskScheduler, muteSender, disableSender, cooldownSender, proxyRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(ChatHytaleListener.class);
    }

}
