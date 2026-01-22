package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabModule extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public Message.Tab config() {
        return fileFacade.message().tab();
    }

    @Override
    public Permission.Message.Tab permission() {
        return fileFacade.permission().message().tab();
    }

}
