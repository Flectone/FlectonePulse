package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;

public abstract class AnvilModule extends AbstractModule {

    private final Message.Anvil message;
    private final Permission.Message.Anvil permission;

    @Inject
    public AnvilModule(FileResolver fileResolver) {
        message = fileResolver.getMessage().getAnvil();
        permission = fileResolver.getPermission().getMessage().getAnvil();
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
