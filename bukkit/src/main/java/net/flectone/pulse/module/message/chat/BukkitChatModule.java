package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatBukkitListener;
import net.flectone.pulse.module.message.chat.listener.ChatPaperListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitChatModule extends ChatModule {

    private final FPlayerService fPlayerService;
    private final BukkitListenerRegistry listenerRegistry;
    private final ReflectionResolver reflectionResolver;
    private final FLogger fLogger;

    @Inject
    protected BukkitChatModule(FileFacade fileFacade,
                               FPlayerService fPlayerService,
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
                               FLogger fLogger,
                               ProxyRegistry proxyRegistry) {
        super(fileFacade, fPlayerService, platformServerAdapter, permissionChecker,
                integrationModule, bubbleModuleProvider, spyModuleProvider, listenerRegistry,
                taskScheduler, muteSender, disableSender, cooldownSender, proxyRegistry);

        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.reflectionResolver = reflectionResolver;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Message.Chat.Mode mode = config().mode();
        if (mode == Message.Chat.Mode.PACKET) return; // already registered in super class
        if (mode == Message.Chat.Mode.PAPER) {
            if (reflectionResolver.hasClass("io.papermc.paper.event.player.AsyncChatEvent")) {
                ChatPaperListener chatPaperListener = new ChatPaperListener(fPlayerService, this);
                listenerRegistry.register(chatPaperListener, EventPriority.valueOf(config().priority().name()));
                return;
            }

            fLogger.warning("It is not possible to use chat in PAPER mode on your server. BUKKIT mode is currently in use.");
        }

        listenerRegistry.register(ChatBukkitListener.class, config().priority());
    }
}
