package net.flectone.pulse.module.message.tab;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class TabModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final PacketProvider packetProvider;
    private final FLogger fLogger;

    @Inject
    public TabModule(FileResolver fileResolver,
                     PacketProvider packetProvider,
                     FLogger fLogger) {
        this.fileResolver = fileResolver;
        this.packetProvider = packetProvider;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                && packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_9_4)) {
            fLogger.warning("TAB module is not supported on this version of Minecraft");
            addPredicate(fEntity -> false);
            return;
        }

        registerModulePermission(permission());

        addChildren(FooterModule.class);
        addChildren(HeaderModule.class);
        addChildren(PlayerlistnameModule.class);
    }

    @Override
    public Message.Tab config() {
        return fileResolver.getMessage().getTab();
    }

    @Override
    public Permission.Message.Tab permission() {
        return fileResolver.getPermission().getMessage().getTab();
    }
}
