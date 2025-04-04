package net.flectone.pulse.module.message.status.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;

@Singleton
public class VersionModule extends AbstractModuleMessage<Localization.Message.Status.Version> {

    @Getter private final Message.Status.Version message;
    private final Permission.Message.Status.Version permission;

    @Inject
    public VersionModule(FileManager fileManager) {
        super(module -> module.getMessage().getStatus().getVersion());

        message = fileManager.getMessage().getStatus().getVersion();
        permission = fileManager.getPermission().getMessage().getStatus().getVersion();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String get(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return resolveLocalization(fPlayer).getName();
    }
}
