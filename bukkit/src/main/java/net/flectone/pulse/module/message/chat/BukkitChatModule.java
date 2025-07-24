package net.flectone.pulse.module.message.chat;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.formatter.TimeFormatter;
import net.flectone.pulse.module.command.spy.SpyModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.listener.ChatListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitChatModule extends ChatModule {

    private final Message.Chat message;
    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    protected BukkitChatModule(FileResolver fileResolver,
                               FPlayerService fPlayerService,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PermissionChecker permissionChecker,
                               IntegrationModule integrationModule,
                               TimeFormatter timeFormatter,
                               Provider<BubbleModule> bubbleModuleProvider,
                               Provider<SpyModule> spyModuleProvider,
                               BukkitListenerRegistry bukkitListenerRegistry) {
        super(fileResolver, fPlayerService, platformPlayerAdapter, permissionChecker, integrationModule, timeFormatter, bubbleModuleProvider, spyModuleProvider);

        this.message = fileResolver.getMessage().getChat();
        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerRegistry.register(ChatListener.class, EventPriority.valueOf(message.getEventPriority().name()));
    }
}
