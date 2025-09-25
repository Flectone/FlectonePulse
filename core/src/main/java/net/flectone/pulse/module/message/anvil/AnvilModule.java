package net.flectone.pulse.module.message.anvil;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;

public abstract class AnvilModule extends AbstractModule {

    private final FileResolver fileResolver;

    protected AnvilModule(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());
    }

    @Override
    public Message.Anvil config() {
        return fileResolver.getMessage().getAnvil();
    }

    @Override
    public Permission.Message.Anvil permission() {
        return fileResolver.getPermission().getMessage().getAnvil();
    }

    public abstract boolean format(FPlayer fPlayer, Object itemMeta);
}
