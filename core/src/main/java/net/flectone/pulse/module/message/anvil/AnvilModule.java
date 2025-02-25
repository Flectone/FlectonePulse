package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;

public abstract class AnvilModule extends AbstractModule {

    private final Message.Anvil message;
    private final Permission.Message.Anvil permission;

    @Inject
    public AnvilModule(FileManager fileManager) {
        message = fileManager.getMessage().getAnvil();
        permission = fileManager.getPermission().getMessage().getAnvil();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract boolean format(FPlayer fPlayer, Object itemMeta);
}
