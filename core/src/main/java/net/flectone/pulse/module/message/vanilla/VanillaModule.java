package net.flectone.pulse.module.message.vanilla;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public abstract class VanillaModule implements ModuleLocalization<Localization.Message.Vanilla> {

    public static final String ARGUMENT = "argument";

    private final FileFacade fileFacade;

    protected VanillaModule(FileFacade fileFacade) {
        this.fileFacade = fileFacade;
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_VANILLA;
    }

    @Override
    public Message.Vanilla config() {
        return fileFacade.message().vanilla();
    }

    @Override
    public Permission.Message.Vanilla permission() {
        return fileFacade.permission().message().vanilla();
    }

    @Override
    public Localization.Message.Vanilla localization(FEntity sender) {
        return fileFacade.localization(sender).message().vanilla();
    }

    public abstract TagResolver argumentTag(FPlayer fResolver, ParsedComponent parsedComponent);

}
