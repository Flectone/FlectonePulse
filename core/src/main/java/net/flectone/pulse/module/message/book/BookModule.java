package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BookModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_BOOK;
    }

    @Override
    public Message.Book config() {
        return fileFacade.message().book();
    }

    @Override
    public Permission.Message.Book permission() {
        return fileFacade.permission().message().book();
    }

    public Optional<String> legacyFormat(FPlayer fPlayer, String string) {
        if (moduleController.isDisabledFor(this, fPlayer)) return Optional.empty();
        if (StringUtils.isEmpty(string)) return Optional.empty();

        return messagePipeline.buildLegacy(fPlayer, string);
    }

    public Optional<String> paperFormat(FPlayer fPlayer, String string) {
        if (moduleController.isDisabledFor(this, fPlayer)) return Optional.empty();
        if (StringUtils.isEmpty(string)) return Optional.empty();

        return Optional.of(messagePipeline.buildJson(MessageContext.builder()
                .sender(fPlayer)
                .message(string)
                .flags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.OBJECT_DEFAULT_VALUE},
                        new boolean[]{true, !config().allowObject()}
                )
                .build()
        ));
    }

}

