package net.flectone.pulse.module.command.chatsetting;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.module.command.chatsetting.builder.DialogMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.InventoryMenuBuilder;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftChatSettingModule extends ChatsettingModule {

    private final Provider<DialogMenuBuilder> dialogMenuBuilderProvider;
    private final Provider<InventoryMenuBuilder> inventoryMenuBuilderProvider;
    private final boolean isNewerThanOrEqualsV_1_21_6;

    @Inject
    public MinecraftChatSettingModule(FileFacade fileFacade,
                                      FPlayerService fPlayerService,
                                      PermissionChecker permissionChecker,
                                      CommandParserProvider commandParserProvider,
                                      ProxySender proxySender,
                                      ProxyRegistry proxyRegistry,
                                      SoundPlayer soundPlayer,
                                      Provider<DialogMenuBuilder> dialogMenuBuilderProvider,
                                      Provider<InventoryMenuBuilder> inventoryMenuBuilderProvider,
                                      @Named("isNewerThanOrEqualsV_1_21_6") boolean isNewerThanOrEqualsV_1_21_6) {
        super(fileFacade, fPlayerService, permissionChecker, commandParserProvider, proxySender, proxyRegistry, soundPlayer);

        this.isNewerThanOrEqualsV_1_21_6 = isNewerThanOrEqualsV_1_21_6;
        this.dialogMenuBuilderProvider = dialogMenuBuilderProvider;
        this.inventoryMenuBuilderProvider = inventoryMenuBuilderProvider;
    }


    @Override
    protected MenuBuilder getMenuBuilder() {
        return config().modern().enable() && isNewerThanOrEqualsV_1_21_6
                ? dialogMenuBuilderProvider.get()
                : inventoryMenuBuilderProvider.get();
    }


}
