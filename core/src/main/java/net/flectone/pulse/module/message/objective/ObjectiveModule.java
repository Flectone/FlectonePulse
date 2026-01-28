package net.flectone.pulse.module.message.objective;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectiveModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public Message.Objective config() {
        return fileFacade.message().objective();
    }

    @Override
    public Permission.Message.Objective permission() {
        return fileFacade.permission().message().objective();
    }

}
