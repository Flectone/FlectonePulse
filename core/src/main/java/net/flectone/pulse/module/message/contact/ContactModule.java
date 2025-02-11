package net.flectone.pulse.module.message.contact;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.contact.afk.AfkModule;

public abstract class ContactModule extends AbstractModule {

    private final Message.Contact message;
    private final Permission.Message.Contact permission;

    public ContactModule(FileManager fileManager) {
        message = fileManager.getMessage().getContact();
        permission = fileManager.getPermission().getMessage().getContact();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        addChildren(AfkModule.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

}
