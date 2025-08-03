package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatBukkitListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;

@Singleton
public class BukkitChatModule extends ChatModule {

    private final Message.Chat message;
    private final ListenerRegistry listenerRegistry;

    @Inject
    protected BukkitChatModule(FileResolver fileResolver,
                               FPlayerService fPlayerService,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PlatformServerAdapter platformServerAdapter,
                               PermissionChecker permissionChecker,
                               IntegrationModule integrationModule,
                               TimeFormatter timeFormatter,
                               Provider<BubbleModule> bubbleModuleProvider,
                               Provider<SpyModule> spyModuleProvider,
                               ListenerRegistry listenerRegistry) {
        super(fileResolver, fPlayerService, platformPlayerAdapter, platformServerAdapter, permissionChecker,
                integrationModule, timeFormatter, bubbleModuleProvider, spyModuleProvider, listenerRegistry);

        this.message = fileResolver.getMessage().getChat();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!message.isPacketBased()) {
            listenerRegistry.register(ChatBukkitListener.class, message.getPriority());
        }
    }
}
