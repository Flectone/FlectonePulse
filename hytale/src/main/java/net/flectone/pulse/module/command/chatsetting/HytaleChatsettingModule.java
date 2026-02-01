package net.flectone.pulse.module.command.chatsetting;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.chatsetting.builder.HytaleMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class HytaleChatsettingModule extends ChatsettingModule {

    private final Provider<HytaleMenuBuilder> hytaleMenuBuilderProvider;

    @Inject
    public HytaleChatsettingModule(FileFacade fileFacade,
                                   FPlayerService fPlayerService,
                                   PermissionChecker permissionChecker,
                                   CommandParserProvider commandParserProvider,
                                   ProxySender proxySender,
                                   ProxyRegistry proxyRegistry,
                                   SoundPlayer soundPlayer,
                                   TaskScheduler taskScheduler,
                                   Provider<HytaleMenuBuilder> hytaleMenuBuilderProvider) {
        super(fileFacade, fPlayerService, permissionChecker, commandParserProvider, proxySender, proxyRegistry, soundPlayer, taskScheduler);

        this.hytaleMenuBuilderProvider = hytaleMenuBuilderProvider;
    }

    @Override
    protected MenuBuilder getMenuBuilder() {
        return hytaleMenuBuilderProvider.get();
    }

}
