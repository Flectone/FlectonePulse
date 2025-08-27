package net.flectone.pulse.module.message.status.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class VersionModule extends AbstractModuleLocalization<Localization.Message.Status.Version> {

    @Getter private final Message.Status.Version message;
    private final Permission.Message.Status.Version permission;

    @Inject
    public VersionModule(FileResolver fileResolver) {
        super(module -> module.getMessage().getStatus().getVersion(), MessageType.VERSION);

        this.message = fileResolver.getMessage().getStatus().getVersion();
        this.permission = fileResolver.getPermission().getMessage().getStatus().getVersion();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public String get(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return resolveLocalization(fPlayer).getName();
    }
}
