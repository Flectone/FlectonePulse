package net.flectone.pulse.module.message.status.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;

@Singleton
public class VersionModule extends AbstractModuleLocalization<Localization.Message.Status.Version> {

    @Getter private final Message.Status.Version message;
    private final Permission.Message.Status.Version permission;

    @Inject
    public VersionModule(FileResolver fileResolver) {
        super(module -> module.getMessage().getStatus().getVersion());

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
        if (checkModulePredicates(fPlayer)) return null;

        return resolveLocalization(fPlayer).getName();
    }
}
