package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AnvilModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MessagePipeline messagePipeline;

    @Override
    public Message.Anvil config() {
        return fileResolver.getMessage().getAnvil();
    }

    @Override
    public Permission.Message.Anvil permission() {
        return fileResolver.getPermission().getMessage().getAnvil();
    }

    public Optional<String> format(FPlayer fPlayer, String string) {
        if (isModuleDisabledFor(fPlayer)) return Optional.empty();
        if (StringUtils.isEmpty(string)) return Optional.empty();

        return messagePipeline.legacyFormat(fPlayer, string);
    }
}
