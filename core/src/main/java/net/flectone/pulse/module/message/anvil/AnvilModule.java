package net.flectone.pulse.module.message.anvil;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;

public abstract class AnvilModule extends AbstractModule {

    private final Message.Anvil message;
    private final Permission.Message.Anvil permission;

    protected AnvilModule(FileResolver fileResolver) {
        this.message = fileResolver.getMessage().getAnvil();
        this.permission = fileResolver.getPermission().getMessage().getAnvil();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract boolean format(FPlayer fPlayer, Object itemMeta);
}
