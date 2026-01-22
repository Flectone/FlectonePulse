package net.flectone.pulse.module.message.status;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StatusModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public Message.Status config() {
        return fileFacade.message().status();
    }

    @Override
    public Permission.Message.Status permission() {
        return fileFacade.permission().message().status();
    }

}
