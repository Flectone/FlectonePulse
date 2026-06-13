package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.BukkitChatListener;
import net.flectone.pulse.module.message.chat.listener.PaperChatListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitChatModule extends MinecraftChatModule {

    private final BukkitListenerRegistry listenerRegistry;
    private final ReflectionResolver reflectionResolver;

    @Inject
    protected BukkitChatModule(FileFacade fileFacade,
                               FPlayerService fPlayerService,
                               SocialService socialService,
                               PlatformServerAdapter platformServerAdapter,
                               PermissionChecker permissionChecker,
                               IntegrationModule integrationModule,
                               Provider<BubbleModule> bubbleModuleProvider,
                               Provider<SpyModule> spyModuleProvider,
                               BukkitListenerRegistry listenerRegistry,
                               TaskScheduler taskScheduler,
                               ReflectionResolver reflectionResolver,
                               MuteSender muteSender,
                               DisableSender disableSender,
                               CooldownSender cooldownSender,
                               MessageDispatcher messageDispatcher,
                               ProxyRegistry proxyRegistry) {
        super(fileFacade, fPlayerService, socialService, platformServerAdapter, permissionChecker,
                integrationModule, bubbleModuleProvider, spyModuleProvider, listenerRegistry,
                taskScheduler, muteSender, disableSender, cooldownSender, messageDispatcher, proxyRegistry);

        this.listenerRegistry = listenerRegistry;
        this.reflectionResolver = reflectionResolver;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Message.Chat.Mode mode = config().mode();
        if (mode == Message.Chat.Mode.PACKET) return; // already registered in super class

        // check paper mode
        if (mode == Message.Chat.Mode.PAPER && reflectionResolver.hasClass("io.papermc.paper.event.player.AsyncChatEvent")) {
            listenerRegistry.register(PaperChatListener.class, config().priority());
        } else {
            listenerRegistry.register(BukkitChatListener.class, config().priority());
        }
    }
}
