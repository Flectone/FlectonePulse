package net.flectone.pulse.module.command.chatsetting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

    @Inject
    public HytaleChatsettingModule(FileFacade fileFacade,
                                   FPlayerService fPlayerService,
                                   PermissionChecker permissionChecker,
                                   CommandParserProvider commandParserProvider,
                                   ProxySender proxySender,
                                   ProxyRegistry proxyRegistry,
                                   SoundPlayer soundPlayer) {
        super(fileFacade, fPlayerService, permissionChecker, commandParserProvider, proxySender, proxyRegistry, soundPlayer);
    }

    @Override
    protected MenuBuilder getMenuBuilder() {
        return null;
    }

}
