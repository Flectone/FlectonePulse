package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BookModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;

    @Override
    public Message.Book config() {
        return fileFacade.message().book();
    }

    @Override
    public Permission.Message.Book permission() {
        return fileFacade.permission().message().book();
    }

    public Optional<String> format(FPlayer fPlayer, String string) {
        if (isModuleDisabledFor(fPlayer)) return Optional.empty();
        if (StringUtils.isEmpty(string)) return Optional.empty();

        return messagePipeline.legacyFormat(fPlayer, string);
    }
}

