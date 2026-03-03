package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnvilModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_ANVIL;
    }

    @Override
    public Message.Anvil config() {
        return fileFacade.message().anvil();
    }

    @Override
    public Permission.Message.Anvil permission() {
        return fileFacade.permission().message().anvil();
    }

    public Optional<String> format(FPlayer fPlayer, String string) {
        if (moduleController.isDisabledFor(this, fPlayer)) return Optional.empty();
        if (StringUtils.isEmpty(string)) return Optional.empty();

        return messagePipeline.legacyFormat(fPlayer, string);
    }
}
