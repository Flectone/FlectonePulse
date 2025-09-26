package net.flectone.pulse.module.message.status.version;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class VersionModule extends AbstractModuleLocalization<Localization.Message.Status.Version> {

    private final FileResolver fileResolver;

    @Inject
    public VersionModule(FileResolver fileResolver) {
        super(MessageType.VERSION);

        this.fileResolver = fileResolver;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());
    }

    @Override
    public Message.Status.Version config() {
        return fileResolver.getMessage().getStatus().getVersion();
    }

    @Override
    public Permission.Message.Status.Version permission() {
        return fileResolver.getPermission().getMessage().getStatus().getVersion();
    }

    @Override
    public Localization.Message.Status.Version localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getStatus().getVersion();
    }

    public String get(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return localization(fPlayer).getName();
    }
}
